package com.spark.ml.util

import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.{DataFrame, SparkSession}

object MLUtils {

  /**
    * 提取特征 - 分词 + 向量化
    * @param dataFrame
    * @param numFeatures
    * @return
    */
  def hashingFeatures(dataFrame: DataFrame, numFeatures: Int): DataFrame = {
    hashingFeatures(dataFrame, numFeatures, "features")
  }

  /**
    * 提取特征公共方法 - 分词 + 向量化
    * @param dataFrame
    * @param numFeatures
    * @param outputCol
    * @return
    */
  def hashingFeatures(dataFrame: DataFrame, numFeatures: Int,outputCol: String): DataFrame = {
    /**
      * 分词
      */
    val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
    val wordsData = tokenizer.transform(dataFrame)

    /**
      * 向量化
      */
    val hashingTF = new HashingTF().setInputCol(tokenizer.getOutputCol).setOutputCol(outputCol).setNumFeatures(numFeatures)
    val featurizedData = hashingTF.transform(wordsData)

    featurizedData
  }

  /**
    * 提取特征 - 分词 + 向量化 + IDF算法
    * @param dataFrame
    * @param numFeatures
    * @return
    */
  def idfFeatures(dataFrame: DataFrame, numFeatures: Int): DataFrame = {
//    /**
//      * 分词
//      */
//    val tokenizer = new Tokenizer().setInputCol("text").setOutputCol("words")
//    val wordsData = tokenizer.transform(dataFrame)
//
//    /**
//      * 向量化
//      */
//    val hashingTF = new HashingTF().setInputCol(tokenizer.getOutputCol).setOutputCol("rawFeatures").setNumFeatures(numFeatures)
//    val featurizedData = hashingTF.transform(wordsData)

    /**
      * 分词 + 向量化
      */
    val featurizedData = hashingFeatures(dataFrame, numFeatures, "rawFeatures")

    /**
      * TF-IDF
      */
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedData)
    val rescaledData = idfModel.transform(featurizedData)

    // rescaledData.show(false)

    rescaledData
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().master("local").appName(s"${this.getClass.getSimpleName}").getOrCreate()

    val numFeatures = 10000

    /**
      * 训练集
      */
    val trainingDataFrame = spark.createDataFrame(TrainingUtils.trainingData).toDF("id", "text", "label")
    /**
      * 分词,向量化,IDF
      */
    val training = MLUtils.idfFeatures(trainingDataFrame, numFeatures).select("label", "features")

    training.show(false)

    spark.stop()
  }

}
