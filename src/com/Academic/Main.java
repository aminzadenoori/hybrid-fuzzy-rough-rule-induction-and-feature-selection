package com.Academic;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Mohammad Amin Zadeh Noori(amin.zadenoori@gmail.com)
 *
 */

public class Main {
    private static  String DATA_SET = "";
    public static int[] mClasses=new int[178];
    public static double[][] mFeatures=new double[178][13];

    public static void main(String[] args) throws IOException {
        if(args.length<1)//checking the input arguments and if it's empty the program will be terminated
        {
            System.out.println("There is no specified wine data set");
            System.exit(0);
        }
        DATA_SET=args[0];
        readCSV(DATA_SET);//reading CSV file and storing the data set in two arrays of features and classes
        HybridFuzzyRough hybridFuzzyRough=new HybridFuzzyRough();//creating new object of HybridFuzzyRough class
        hybridFuzzyRough.mClasses=mClasses;
        hybridFuzzyRough.mData=mFeatures;
        hybridFuzzyRough.mFeatureSize=13;
        hybridFuzzyRough.mSampleSize=178;
        hybridFuzzyRough.calcuateStandardDivision();//calculating and storing standard diviation at the begining of process for run time efficiency
        hybridFuzzyRough.objectsPOSA();//calculating and storing positive region of A set at the beginning of process for run time efficiency
        hybridFuzzyRough.folding();
    }
    /*
    *  reading the file of data set using apache common CSV 1.5
    */
    public static void readCSV(String fileName) throws IOException {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(DATA_SET), StandardCharsets.UTF_8);
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
        ) {


            int index=0;
            for (CSVRecord csvRecord : csvParser) {
                // Accessing Values by Column Index
                mClasses[index]=Integer.parseInt(csvRecord.get(0));

                for(int i=0;i<13;i++){
                    mFeatures[index][i]=Double.parseDouble(csvRecord.get(i+1));
                }
                index++;
            }
        }

    }

}
