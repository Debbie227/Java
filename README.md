# NCBI BLAST Search Tool

This Java program performs a nucleotide BLAST search using the NCBI BLAST API. The tool connects to the European Bioinformatics Institute (EBI) API, queries for sequence matches, and parses the results in XML format. The output is saved as a tab-separated text file containing detailed BLAST results.

## Features

- **Sequence Input**: The program can accept a nucleotide sequence either from a FASTA file or entered manually.
- **BLAST Search**: It connects to the NCBI BLAST service and performs a nucleotide BLAST search.
- **Output File**: The results are written to a text file named after the accession number of the query sequence, followed by `_output.txt`.
- **Parsed Results**: The output is formatted in a tab-separated format with relevant information about each match, including:
  - Query sequence name
  - Query start and end positions
  - Subject accession number
  - Subject start and end positions
  - Subject description
  - Matching score

## Installation

### Prerequisites

- **Java 8 or higher**: The program is written in Java, so you'll need a JDK to compile and run it.
- **BioJava**: The program uses the [BioJava library](https://biojava.org/) for parsing biological sequence data and interacting with the NCBI API.
  
You can include the BioJava dependency in your project by adding it to your `pom.xml` if you're using Maven or by downloading the jar files directly.

#### Example Maven dependency:

```xml
<dependency>
    <groupId>org.biojava</groupId>
    <artifactId>biojava-core</artifactId>
    <version>5.3.0</version>
</dependency>
```

### Setting Up the Project

1. Clone this repository to your local machine:

```bash
git clone https://github.com/Debbie227/ebi-blast-search.git
```

2. Install dependencies (if using Maven):

```bash
mvn install
```

### Usage

The program is run from the command line and accepts no arguments:

```bash
java -jar BlastSearchTool.jar
```

### Input:

- **input_sequence:** The nucleotide sequence to search. This can either be a FASTA file or a manually entered sequence.
    - If it's a file, use the dialog box to find select the file.
    - If it's a manually entered sequence, exit out of the dialog box and provide the sequence when prompted.

### Output:

- **output_file:** The name of the output text file. The results will be saved in a tab-separated format, and the file will be named <accession_number>_output.txt.

### Example Usage:

1. Using a FASTA file:

```bash
java -jar BlastSearchTool.jar input_sequence.fasta output_results.txt
```

2. Using a manually entered sequence:

```bash
    java -jar BlastSearchTool.jar "ATGCGTACGGTGCTAGCTAG" output_results.txt
```

### Example Output Format:

The output file will contain the following columns, separated by tabs:

| query sequence name      | query start position | query end position | subject accession number | subject start position | subject end position | subject description | matching score |
|--------------------------|----------------------|--------------------|--------------------------|------------------------|----------------------|---------------------|----------------|
| by21f03.y1|1    | 381                    | BC117340                | 83                 | 462                     | Homo sapiens crystallin, gamma D, mRNA (cDNA clone MGC:150949 IMAGE:40125891), complete cds.                  | 680 |
| by21f03.y1|1     | 381                    | BC117338                | 83                 | 462                    | Homo sapiens crystallin, gamma D, mRNA (cDNA clone MGC:150947 IMAGE:40125889), complete cds.                  | 680 |

### Columns:

1. **Query Sequence Name:** Name of the input sequence.
2. **Query Start Position:** The starting position of the query in the subject sequence.
3. **Query End Position:** The ending position of the query in the subject sequence.
4. **Subject Accession Number:** The accession number of the matching subject sequence.
5. **Subject Start Position:** The start position of the match in the subject sequence.
6. **Subject End Position:** The end position of the match in the subject sequence.
7. **Subject Description:** Description of the matching subject sequence.
8. **Matching Score:** The score for the match.

## Example Project Structure

```
ebi-blast-search/
│
├── src/
│   └── BLASTandParse.java.java       # Main Java file for running the BLAST search
│
├── lib/
│   └── biojava-core-5.3.0.jar    # BioJava library (or include via Maven)
│
├── input/
│   └── sample_sequence.fasta     # Example input sequence file
│
├── output/
│   └── sample_output.txt         # Example output file (generated after running the program)
│
├── pom.xml                      # Maven dependency configuration (if using Maven)
└── README.md                    # Project documentation (this file)
```

## Troubleshooting

- **No output generated:** Check if the input sequence is valid and ensure that BioJava is correctly installed.
- **Error in API connection:** Ensure that your system has internet access and the NCBI BLAST API is available.
- **Invalid input sequence:** If manually entering a sequence, ensure that it is in the correct nucleotide format (e.g., "ATGC").

## License

This project is licensed under the MIT License - see the [LICENSE](https://opensource.org/license/mit) file for details.
