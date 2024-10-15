/*
 * Class Name: BIFS618
 * Homework Final, question35
 * File name:BLASTandParse.java
 * Program author name: Debra Pacheco
 * Date: 08/05/2024
*/


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.xml.stream.XMLStreamException;

import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.xml.sax.SAXException;

/*Welcome to Deb's EBI client BLAST search and parser.
 * Biojava is a required dependancy for this program.
 * This program accepts a FASTA formatted file and performs a blast search on DNA.
 * Only the first FASTA entry in the file will be processed.
 * Fasta sequence can be manually entered by cancelling out of the dialog box.
 * If there are multiple alignments for a BLAST hit the last alignment will be returned.
 * Results are saved in tab separated format to a local text file named with the sequence name.
 * The search uses all EMBL nucleotide databases and may take a while to complete.
 */

 public class BLASTandParse {

    private static final String NCBI_BLAST_URL = "https://www.ebi.ac.uk/Tools/services/rest/ncbiblast";
    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final JFileChooser FILE_CHOOSER = new JFileChooser();
    private static final String userEmail = "dpacheco4@student.umgc.edu";
    
    public static void main(String[] args) {
        System.out.println("          Welcome to Deb's EBI client BLAST search and parser.\n");
        System.out.println("        *******************************************************************        ");
        System.out.println("This program accepts a FASTA formatted file or manual entry and performs a blast search on DNA.");
        System.out.println("             ***Please cancel out of the file dialog box for manual entry.***");
        System.out.println("                Only the first FASTA entry in a file will be processed.");
        System.out.println("     If there are multiple alignments for a BLAST hit the last alignment will be returned.");
        System.out.println("  Results are sent in tab separated format to a local text file named with the sequence name.");
        System.out.println("     *******************************************************************     ");
        System.out.println("\nThe search uses all EMBL nucleotide databases and may take a while to complete.\n\n");

            
        String seqID = "";
        String seq = "";
        
        //Open a FASTA file
        try {
            BufferedReader br = openFile();
                      
            RichSequenceIterator it = IOTools.readFastaDNA(br, null);

           //Allow for file to be bypassed and DNA entered directly in console
            if (br == null) {
                System.out.println("No file selected.\n");
                System.out.println("Please manually enter ID.");
                
                try (Scanner sc = new Scanner(System.in)) {
					seqID = sc.nextLine(); 
					
					System.out.println("Please manually enter sequence.");
					seq = sc.nextLine();
				} 
            }else {
            	//Read in first entry from FASTA file
                if (it.hasNext()) {
                    RichSequence s = it.nextRichSequence();
                    seqID = s.getAccession();
                    seq = s.seqString();
                }
            }
            
            //Start the BLAST search
            String jobID = performBLAST(seq);
            System.out.println("Your Job Dispatcher Job Id is:");
            System.out.println(jobID);

            // Wait for the results to be ready
            waitForResults(jobID);

            // Retrieve and process the results
            ArrayList<String> output = resultsRead(jobID, seqID);
            outputToFile(output, seqID);
            
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

//Sends data and creates a job for the BLAST search
private static String performBLAST(String seq) throws IOException {
    URL url = new URL(NCBI_BLAST_URL + "/run");
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoInput(true);
    connection.setDoOutput(true);
    connection.setRequestProperty("accept", "text/plain;charset=UTF-8");
    connection.setRequestProperty("content-type", CONTENT_TYPE);

    String parameters = "sequence=" + seq + "&program=blastn&database=em_all&stype=dna&email=" + userEmail;
    try (OutputStream outputStream = connection.getOutputStream()) {
        outputStream.write(parameters.getBytes());
        outputStream.flush();
    } catch (Exception e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
    }


    String jobID = "";
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        jobID = reader.readLine().strip();

    }catch (Exception e) {
        System.out.println("An error occurred.");
        e.printStackTrace();
    }

    connection.disconnect();
    return jobID;
}

    //waits for results from blast search to be finished
    private static void waitForResults(String jobID) throws IOException, InterruptedException {
        String status = "";
        
        status = statusConnection(jobID);
        
        System.out.println("\nYour results are pending. This may take some time.");

        while (!(status.contains("FINISHED"))) {
            System.out.println("\nJob not completed, checking again in 5 minutes.");
            Thread.sleep(5 * 60 * 1000); // Wait for 5 minutes
            
            status = statusConnection(jobID);

            }
        System.out.println("\nYour results are ready.");        
    }

    //Retrieves status results for the job
    private static String statusConnection(String jobID) throws IOException, InterruptedException {
    	String statusURL = NCBI_BLAST_URL + "/status/" + jobID;
        HttpURLConnection connection = (HttpURLConnection) new URL(statusURL).openConnection();
        connection.setRequestMethod("GET");

        String status = "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            status = reader.readLine().strip();
            System.out.println("\n" + status);
        }catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
		return status;
    }

        //Parser that retrieves and extracts results from BLAST XML stream - formatted for required tab separated file as output.
	private static ArrayList<String> resultsRead(String ID, String seqID) throws IOException, SAXException, XMLStreamException {		
		
        //connect to the NCBI website to retrieve results
        URL url = new URL(NCBI_BLAST_URL + "/result/" + ID + "/xml");
        HttpURLConnection connect = (HttpURLConnection) url.openConnection();
        connect.setRequestMethod("GET");

        /* 
         * Final results expected 
         * 
         */
        
        //initialize variables
        String line = "";
        String hit[] = new String[8];
        ArrayList<String> output = new ArrayList<String>();
        
        hit[0] = seqID;
        
		//Read the input XML from NCBI
		BufferedReader br = new BufferedReader(new InputStreamReader(connect.getInputStream()));
		while (br.ready()) {
		
			line = br.readLine();
	
			//Required data is inside XML start tags - SAX parser cannot retrieve information
			//Parse the XML one line at a time to retrieve the requested data
			if (line.contains("hit number=")){
		
				Pattern id = Pattern.compile("id=");
				Matcher i = id.matcher(line);
				i.find();
				Pattern ac = Pattern.compile("ac=");
				Matcher a = ac.matcher(line);
				a.find();
				Pattern desc = Pattern.compile("description=");
				Matcher d = desc.matcher(line);
				d.find();
		
		
		        hit[3] = line.substring(i.end(),a.start()-1);
		        hit[6] = line.substring(d.end(), line.length());


			}
			else if (line.contains("<score>")){
				Pattern sc = Pattern.compile(">\\d+<");
				Matcher s = sc.matcher(line);
				s.find();
		
				hit[7] = line.substring(s.start(), s.end());

			}
			else if (line.contains("<querySeq")) {

				Pattern start = Pattern.compile("start=");
				Matcher s = start.matcher(line);
				s.find();
				Pattern end = Pattern.compile("end=");
				Matcher e = end.matcher(line);
				e.find();
				Pattern seq = Pattern.compile("\">");
				Matcher se = seq.matcher(line);
				se.find();
		
				hit[1] = line.substring(s.end(),e.start()-1);
				hit[2] = line.substring(e.end(),se.start());


			}
			else if (line.contains("<matchSeq")) {

				Pattern start = Pattern.compile("start=");
				Matcher s = start.matcher(line);
				s.find();
				Pattern end = Pattern.compile("end=");
				Matcher e = end.matcher(line);
				e.find();
				Pattern seq = Pattern.compile("\">");
				Matcher se = seq.matcher(line);
				se.find();
		
				hit[4] = line.substring(s.end(),e.start()-1);
				hit[5] = line.substring(e.end(),se.start());

			}
	
			//Clean up the data and add each completed tab separated entry to a list
			else if (line.contains("</hit>")) {

				String values = "";
		
				for(int i=0; i<8; i++) {
					values += (hit[i] + "\t");
				}
				values = values.replace("\"", "");
				values = values.replace(">", "");
				values = values.replace("<", "");
				values = values.strip();
				output.add(values);

			}

		}
		br.close();
	
		return output;
		
		}

    //Writes selected results from BLAST search to file
    private static void outputToFile(ArrayList<String> output, String seqID) {
        String filepath = seqID.substring(0, Math.min(8, seqID.length())) + "_Output.txt";
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            for (String s : output) {
                writer.write(s);
                writer.newLine();
            }
            System.out.println("Output written to file: " + filepath);
        } catch (IOException e) {
        	System.out.println("Error writing file.");
            e.printStackTrace();
        }
    }

    //File opener
    private static BufferedReader openFile() {
        int result = FILE_CHOOSER.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = FILE_CHOOSER.getSelectedFile();
            try {
                return new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
            	System.out.println("File not found.");
                e.printStackTrace();
            }
        }
        return null;
    }
}
