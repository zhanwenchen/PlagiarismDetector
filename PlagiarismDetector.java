/*
* @author Zhanwen "Phil" Chen
* TripAdvisor Coding Assessment
* November 17, 2016
*/

import java.util.*;
import java.io.*;

public class PlagiarismDetector {

     String synsFileName, file1FileName, file2FileName;
     List<String[]> syns, file1Words, file2Words;
     String[] file1Tuples;
     List<String> file2Tuples;
     int n;
     double score;

     // Constructor takes and sets file names
     public PlagiarismDetector(String synsFileName,
                               String file1FileName,
                               String file2FileName,
                               int n) {
          this.synsFileName = synsFileName;
          this.file1FileName = file1FileName;
          this.file2FileName = file2FileName;
          this.n = n;
     }

     public PlagiarismDetector(String synsFileName,
                               String file1FileName,
                               String file2FileName) {
          this.synsFileName = synsFileName;
          this.file1FileName = file1FileName;
          this.file2FileName = file2FileName;
          this.n = 3;
     }

     /*
     * txtToWords
     * Input: fileName. E.g., "syns.txt" ("run sprint jog")
     * Output: An ArrayList of list of words in each line
     */
     private List<String[]> txtToWords(String fileName) {
          String line = null;
          List<String[]> splittedLines = new ArrayList<String[]>();
          try {
               FileReader fileReader = new FileReader(fileName);
               BufferedReader bufferedReader = new BufferedReader(fileReader);
               while((line = bufferedReader.readLine()) != null) {
                    String[] splitedLine = line.split("\\s+");
                    splittedLines.add(splitedLine);
               }
               bufferedReader.close();
          } catch (FileNotFoundException ex) {
               System.out.println("Unable to find file '" + fileName + "'");
          } catch(IOException ex) {
               System.out.println("File read error '" + fileName + "'");
          }
          return splittedLines;
     }

     /*
     * lineToTuples
     * Input: words. E.g., ["go", "for", "a", "run"]
     * Output: ["go for a", "for a run"]
     */
     private String[] lineToTuples(String[] wordsPerLine) {
          // 4 words, N=1 gives 4 tuples, N=2 gives 3 tuples ... generalize
          int numberOfTuples = wordsPerLine.length - this.n + 1;
          String[] tuples = new String[numberOfTuples];
          for (int i = 0; i < numberOfTuples; i++) {
               String tuple = "";
               for (int j = i; j < this.n + i; j++) {
                    // For all but last word in a tuple, append word & space
                    if (j < n + i - 1) {
                         tuple += wordsPerLine[j] + " ";
                    } else {
                         tuple += wordsPerLine[j];
                    }
               }
               tuples[i] = tuple;
          }
          return tuples;
     }

     /*
     * getTupleSimularityScore
     * Input: tuples1. E.g., ["go for a", "for a run"]
     *        tuples2. E.g., ["go for a", "for a jog"]
     * Output: a percentage. E.g., 100%
     */
     private double getTupleSimularityScore() {
          int plagiarismCount = 0;
          for (String s : this.file1Tuples) {
               // Increment count if s is a member of tuples2
               // Equality
               if (this.file2Tuples.contains(s)) {
                    plagiarismCount++;
               }
          }
          return (double)plagiarismCount / (double)this.file1Tuples.length;
     }


     /*
     * lineToFuzzyTuples
     * Input: ["go", "for", "a", "run"]
     * Output: ["go for a", "for a run", "for a sprint", "for a jog"]
     */
     private List<String> lineToFuzzyTuples(String[] wordsPerLine) {
          // fuzzyTuples =
          //[["go for a", "for a run"],
          // ["go for a", "for a sprint"],
          // ["go for a", "for a jog"]]
          List<String[]> fuzzyTuples = new ArrayList<String[]>();

          for (int i=0; i < wordsPerLine.length; i++) {
               for (String[] synsRow : syns) {
                    // For each matched synonym in input line
                    if(Arrays.asList(synsRow).contains(wordsPerLine[i])) {
                         for (String syn : synsRow) {
                              // Avoid repeats
                              if (!wordsPerLine[i].equals(syn)) {
                                   // Replace original with syn
                                   wordsPerLine[i] = syn;
                                   String[] naiveTuples =
                                        lineToTuples(wordsPerLine);
                                   fuzzyTuples.add(naiveTuples);
                              }
                         }
                    }
               }
          }

          // Merge rows in the list and remove duplicates
          List<String> mergedTuples = new ArrayList<String>();
          for (String [] fuzzyTuplesRow : fuzzyTuples) {
               for (String fuzzyTuple : fuzzyTuplesRow) {
                    if (!mergedTuples.contains(fuzzyTuple)) {
                         mergedTuples.add(fuzzyTuple);
                    }
               }
          }
          return mergedTuples;
     }

     public void run() {
          this.syns = this.txtToWords(synsFileName);
          this.file1Words = this.txtToWords(file1FileName);
          this.file2Words = this.txtToWords(file2FileName);
          // Assume file1 and file2 have one line including line wrapping
          this.file1Tuples = this.lineToTuples(file1Words.get(0));
          this.file2Tuples = this.lineToFuzzyTuples(file2Words.get(0));
          this.score = this.getTupleSimularityScore();
          System.out.println(String.format("%.0f%%", this.score * 100));
     }

     public static void main(String args[]) {
          if (args.length == 3) {
               PlagiarismDetector plagiarismDetector =
                    new PlagiarismDetector(args[0], args[1], args[2]);
               plagiarismDetector.run();
          } else if (args.length == 4) {
               PlagiarismDetector plagiarismDetector =
                    new PlagiarismDetector(args[0], args[1],
                         args[2], Integer.parseInt(args[3]));
               plagiarismDetector.run();
          } else {
               System.out.println("PlagiarismDetector takes 3-4 arguments:\n"
               + "1. file name for a list of synonyns\n"
               + "2. input file 1\n"
               + "3. input file 2\n"
               + "4. (optional) the number N, the tuple size. If not supplied,"
               + " the default should be N=3.\n");
          }
     }
 }
