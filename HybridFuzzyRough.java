package com.Academic;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Mohammad Amin Zadeh noori(amin.zadenoori@gmail.com)
 *
 */
public class HybridFuzzyRough {
    public int mFeatureSize;
    public int mSampleSize;
    public double[][] mData;
    public int[] mClasses;
    public static double[] mCoverA;
    public ArrayList<featureRuleHybridization> mFeaturesrules;
    public int[] mFeatureCoverList;
    public double[] mStandarddivsions;
    public double[] coverage;

    /*
    *  Calculation  of standard deviations and storing them in an  array
    **/
    public void calcuateStandardDivision(){
        mStandarddivsions=new double[mFeatureSize];

        for(int a=0;a<mFeatureSize;a++)
        {

            double standardDeviation;
            double sum = 0;
            for (int i = 0; i < mSampleSize; i++)
                sum = mData[i][a] + sum;
            double mean = sum / mSampleSize;
            double temp = 0;
            for (int i = 0; i < mSampleSize; i++)
                temp = temp + Math.pow(mData[i][a] - mean, 2);
            temp = temp / (mSampleSize);
            standardDeviation = Math.sqrt(temp);
            mStandarddivsions[a]=standardDeviation;
        }


    }
    /*
    *  R or a-Indiscernibility
    */
    public double aIndiscernibility(int a,int x,int y){


        double eq1=(mData[y][a]-mData[x][a]) /mStandarddivsions[a];
        double eq2=(mData[x][a]-mData[y][a]) /mStandarddivsions[a];
        return Math.max(Math.min(eq1,eq2)+1,0);

}
    public double aIndiscernibility(int a,int x,int y,double[][] testdata){


        double eq1=(mData[y][a]-testdata[x][a]) /mStandarddivsions[a];
        double eq2=(testdata[x][a]-mData[y][a]) /mStandarddivsions[a];
        return Math.max(Math.min(eq1,eq2)+1,0);

    }
    /*
    *  RB or b-Indiscernibility
    * */
    public double bIndiscernibility(java.util.List<Integer> b, int x, int y){
        /* calculation of standard divation */
       double listOfRs[]=new double[b.size()];
       for(int i=0;i<b.size();i++){
           listOfRs[i]=aIndiscernibility(b.get(i),x,y);
       }
        Arrays.sort(listOfRs);
        return listOfRs[0];
    }
    public double bIndiscernibility(java.util.List<Integer> b, int x, int y,double[][] testdata){
        /* calculation of standard divation */
        double listOfRs[]=new double[b.size()];
        for(int i=0;i<b.size();i++){
            listOfRs[i]=aIndiscernibility(b.get(i),x,y,testdata);
        }
        Arrays.sort(listOfRs);
        return listOfRs[0];
    }
    /*
    *  positive region calculation for set of features b and object y
    * */
    public double positiveRegionB (java.util.List<Integer> b,int y){
        java.util.List<Double> list=new ArrayList();
        int targetclass=mClasses[y];
        for(int i=0;i<mSampleSize;i++){
                if(mClasses[i]==targetclass)
                    list.add(Math.max(1-bIndiscernibility(b,i,y),1));
                else
                    list.add(Math.max(1-bIndiscernibility(b,i,y),0));
        }
        Collections.sort(list);
        return list.get(0);




    }
    /*
    *  positive region calculation for all features or A set for an object y
    * */
    public double positiveRegionA (int y){
        java.util.List<Integer> Aset=new ArrayList();
        for(int k=0;k<mFeatureSize;k++)
            Aset.add(k);
        java.util.List<Double> list=new ArrayList();

        int targetclass=mClasses[y];
        for(int i=0;i<mSampleSize;i++){
            if(mClasses[i]==targetclass)
                list.add(Math.max(1-bIndiscernibility(Aset,i,y),1));
            else
                list.add(Math.max(1-bIndiscernibility(Aset,i,y),0));
        }
        Collections.sort(list);
        return list.get(0);




    }
    /*
    * calculation of degree of dependecy for a set of features B
    * */
    public double degreeOfDependecy( java.util.List<Integer> B){
        double absPOSB=0;
        double absPOSA=0;
        for(int i=0;i<mSampleSize;i++){


            absPOSB+=positiveRegionB(B,i);
            absPOSA+=positiveRegionA(i);


        }
        return absPOSB/absPOSA;

    }
    /*
    * calculation of positive region of set A full region of features
    * */
    public void objectsPOSA(){
        double[] coverA=new  double[mSampleSize];
        for(int i=0;i<mSampleSize;i++)
            coverA[i]=positiveRegionA(i);
        mCoverA=coverA;

    }
    /*
    * calculation of fully covered objects
    * */
    public int[] covered(double[] cov){
        int[] covered=new int[mSampleSize];
        for (int i=0;i<mSampleSize;i++){
            if(cov[i]==mCoverA[i])
                covered[i]=1;
        }
        return covered;
    }
    /*
    * feature selection and rules induction
    * */
    public void quickRules(){
        mFeaturesrules=new ArrayList<>();
        double decisionValue=0;
        double tempdegreeOfDependecy;
        int selectedFeature;
        mFeatureCoverList=new int[mFeatureSize];
        //coverage=new int[mSampleSize];
        Arrays.sort(mFeatureCoverList);
        ArrayList<Integer> newsSet=new ArrayList<>();
        if(mFeatureCoverList[0]==0)
            decisionValue=0;
        double temp=0;
        int tempIndex=0;
        int[] covered;
        coverage=new double[mSampleSize];

        do{


            for(int i=0;i<mFeatureSize;i++){
                if(mFeatureCoverList[i]==0){
                    selectedFeature=i;
                    newsSet=new ArrayList<>();
                    newsSet.add(i);
                    for(int k=0;k<mFeatureSize;k++) {
                        if (mFeatureCoverList[k] == 1)
                            newsSet.add(k);
                    }
                    for(int j=0;j<mSampleSize;j++) {
                        covered=covered(coverage);
                        if (positiveRegionB(newsSet, j) == mCoverA[j] && covered[j]==0){
                            double[] C=new double[mSampleSize];//coverage of rule
                            for(int l=0;l<mSampleSize;l++){
                                C[l]=bIndiscernibility(newsSet,j,l);
                            }
                            check(newsSet,C,mClasses[j],j);

                        }

                    }

                    tempdegreeOfDependecy=degreeOfDependecy(newsSet);
                    if(degreeOfDependecy(newsSet)>temp){
                        temp=tempdegreeOfDependecy;
                        tempIndex=i;
                    }

                }
            }
            mFeatureCoverList[tempIndex]=1;
            decisionValue=degreeOfDependecy(newsSet);
        }
        while (decisionValue!=1);
        int k=0;
    }
    /*
    * feasibility check of induced rules
    * */
    public void check(java.util.List<Integer> features,double[] icoverage,int classLabel,int objectNumber){
        boolean addRule=true;
        boolean subSet=true;
        boolean delete;
        if (mFeaturesrules.size()<1)
            subSet=false;
        for(int i=0;i<mFeaturesrules.size();i++){
            for(int j=0;j<mSampleSize;j++){
               if(icoverage[j]>coverage[j]) {
                   subSet = false;
               }

            }

        }
        if(subSet)
            addRule=false;
        else {
            java.util.List<Integer> delList=new ArrayList<>();
            for (int i = 0; i < mFeaturesrules.size(); i++) {
                delete = true;

                for (int j = 0; j < mSampleSize; j++) {
                    if (icoverage[j] < mFeaturesrules.get(i).mCover[j]) {
                    delete = false;
                    }
                 if(delete)
                    delList.add(i);

                }
            }
            for(int i=0;i<delList.size();i++)
                mFeaturesrules.remove(delList.get(i));
        }
        if(addRule) {
            featureRuleHybridization frh = new featureRuleHybridization();
            frh.mFeatures = features;
            frh.mCover = icoverage;
            frh.mSameClasses = classLabel;
            frh.object=objectNumber;
            mFeaturesrules.add(frh);
            for (int i = 0; i < mSampleSize; i++) {
                coverage[i] = Math.max(coverage[i], icoverage[i]);
            }
        }

    }
    /*
    * selecting  train and test data according to K-fold cross validation by K=5
    * in this method data set is shuffled for better randomised selection  of data
    * */
    public void folding(){
        int temp=0;
        double acctemp=0;
        double[][] dataSet=mData;
        int[] classes=mClasses;
        double[][] trainData=new double[178-35][13];
        double[][] testData=new double[35][13];
        int[] trainClasses=new int[178-35];
        int[] testClasses=new int[35];
        int[] randomJuxtaposition=new int[178];
        int trainIndex;
        int testIndex;
        String datasetTemp;
        for(int i=0;i<178;i++)
            randomJuxtaposition[i]=i;
        shuffleArray(randomJuxtaposition);
        System.out.println();
        System.out.println();
        System.out.println("randomly juxtaposed data set :");
        System.out.println();
        System.out.println();
        for(int i=0;i<178;i++){

            datasetTemp=i+"||class:"+classes[randomJuxtaposition[i]]+",";
            for(int j=0;j<13;j++){
                datasetTemp=datasetTemp+"a"+j+":";
                datasetTemp=datasetTemp+dataSet[randomJuxtaposition[i]][j];
                datasetTemp=datasetTemp+",";

            }
            datasetTemp=datasetTemp.substring(0,datasetTemp.length()-1);
            System.out.println(datasetTemp);
        }

        int[] index=new int[178];
        int[] suggestedlabels;
        double acc;

        for(int i=0;i<5;i++){
            for(int j=i*35;j<(i*35)+35;j++)
                index[j]=1;
             trainIndex=0;
             testIndex=0;
            for(int k=0;k<178;k++){
                if(index[k]==1){
                    testData[testIndex] = dataSet[randomJuxtaposition[k]];
                    testClasses[testIndex]=classes[randomJuxtaposition[k]];
                    testIndex++;
                }
                else {
                    trainData[trainIndex] = dataSet[randomJuxtaposition[k]];
                    trainClasses[trainIndex]=classes[randomJuxtaposition[k]];
                    trainIndex++;
                }

             }
             index=new int[178];
             mData=trainData;
             mClasses=trainClasses;
             mSampleSize=143;
             quickRules();
             suggestedlabels=mamdaniFIS(testData,testClasses);
             acc=0;
             for(int b=0;b<35;b++){
                 if(suggestedlabels[b]==testClasses[b])
                     acc++;

             }
            acc=acc/35;
            acctemp+=acc;
            temp=i+1;
            System.out.println();
            System.out.println();
            System.out.println("fold number "+temp);
            System.out.println("accuracy of this fold is "+acc);
            writeRules(i);
            trainData=new double[178-35][13];
            testData=new double[35][13];
        }
        acctemp=acctemp/5;
        System.out.println("average accuracy is "+acctemp);

    }
    /*
    * implementation of array shuffling algorithm
    * */
    static void shuffleArray(int[] ar) {

        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
    /*
    * implementation of mamdani FIS by the highest firing rate of a rule
    * */
    public int[] mamdaniFIS(double[][] testdata,int[] test){

        int[] sugeestedlabels=new int[35];
        double[] firingValue=new double[mFeaturesrules.size()];
        featureRuleHybridization featureRuleHybridationobject=new featureRuleHybridization();
        double temp;
        int selectedRule=0;
        for(int i=0;i<35;i++) {
            for (int j = 0; j < mFeaturesrules.size(); j++) {
                featureRuleHybridationobject = mFeaturesrules.get(j);
                firingValue[j]=bIndiscernibility(featureRuleHybridationobject.mFeatures,i,featureRuleHybridationobject.object,testdata);
            }
            temp=0;

            for (int j = 0; j < mFeaturesrules.size(); j++) {
                if(firingValue[j]>temp){
                    temp=firingValue[j];
                    selectedRule=j;

                }
                sugeestedlabels[i]=mFeaturesrules.get(selectedRule).mSameClasses;

            }
        }



        return  sugeestedlabels;


    }
    /*
    * write selected features and rules to the console
    * */
    public void writeRules(int foldnumber){
        String temp="";
        String value="";
        foldnumber++;

         System.out.println (" Rules and selected features for fold number  " + foldnumber);
        System.out.println("--------------------------------------------------------------------------------");
         for(int i=0;i<13;i++){
             if(mFeatureCoverList[i]==1){
                 temp=temp+i;
                 temp+=",";
             }
         }
        temp=temp.substring(0,temp.length()-1);
        System.out.println("Selected features are {"+temp+"}");
        System.out.println("--------------------------------------------------------------------------------");

         featureRuleHybridization featureRuleHybridationobject;
         for(int i=0;i<mFeaturesrules.size();i++){
             featureRuleHybridationobject=mFeaturesrules.get(i);
             temp="";
             value="";
             for(int j=0;j<featureRuleHybridationobject.mFeatures.size();j++){
                 temp =temp + featureRuleHybridationobject.mFeatures.get(j).toString();
                 temp+=',';
                 value=value+mData[featureRuleHybridationobject.object][featureRuleHybridationobject.mFeatures.get(j)];
                 value+=',';

             }
             temp=temp.substring(0,temp.length()-1);
             value=value.substring(0,value.length()-1);
             System.out.println("{"+temp+"},values of features{"+value+"},ClassOfObject:"+featureRuleHybridationobject.mSameClasses);


         }
        System.out.println("............................................................................................................................");


    }
}
