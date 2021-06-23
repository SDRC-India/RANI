//package org.sdrc.datum19.service;
//
//import static org.apache.spark.sql.functions.col;
//import static org.apache.spark.sql.functions.lower;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.ml.clustering.KMeans;
//import org.apache.spark.ml.clustering.KMeansModel;
//import org.apache.spark.ml.evaluation.ClusteringEvaluator;
//import org.apache.spark.ml.feature.NGram;
//import org.apache.spark.ml.feature.OneHotEncoderEstimator;
//import org.apache.spark.ml.feature.StopWordsRemover;
//import org.apache.spark.ml.feature.StringIndexer;
//import org.apache.spark.ml.feature.Tokenizer;
//import org.apache.spark.ml.feature.VectorAssembler;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.SparkSession;
//import org.apache.spark.sql.functions;
//import org.sdrc.datum19.document.RelatedIndicators;
//import org.sdrc.datum19.repository.RelatedIndicatorsRepository;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.mongodb.spark.MongoSpark;
//
///*
// * author : Biswabhusan Pradhan
// * email : biswabhusan@sdrc.co.in
// * description : this service deals with grouping of indicators using an unsupervised learning algorithm.
// */
//@Service
//public class IndicatorClassificationService {
//
//	
//	@Autowired
//	private RelatedIndicatorsRepository relatedIndicatorsRepository;
//	
//	/*
//	 * author : Biswabhusan Pradhan
//	 * email : biswabhusan@sdrc.co.in
//	 */
//	public void classifyIndicators() {
//		// TODO Auto-generated method stub
//		System.setProperty("hadoop.home.dir", "C:\\Program Files\\Hadoop");
//		Logger.getLogger("org.apache").setLevel(Level.ERROR);
//		
//		//Creating the spark session and configuring
//		SparkSession spark = SparkSession.builder()
//				.appName("Indicator Clustering")
//				.config("spark.warehouse.dir", "file:///c:/tmp/")
//				.master("local[*]")
//				.getOrCreate();
//		
//		//Loading the dataset
//		Dataset<Row> indicatorData = spark.read()
//				.option("header", true)
//				.option("inferSchema", true)
//				.csv("E:\\rani-workspace\\indicator1.csv");
//		indicatorData.show();
//		
//		indicatorData = indicatorData.select("indicatorNid", "indicatorName", "unit", "subgroup", "formId", "numerator")
//				.where(col("subgroup").isNotNull().and(col("unit").isNotNull()).and(col("unit").equalTo("number")).and(col("numerator").isNotNull()))
//				.withColumn("formId", functions.when(col("formId").isNull(), 0).otherwise(col("formId")))
//				.withColumn("indicatorName", lower(col("indicatorName")));
//		
//		//String indexing the text fields into numeric forms
//		indicatorData = new StringIndexer()
//				.setInputCol("subgroup")
//				.setOutputCol("subgroup_index")
//				.fit(indicatorData)
//				.transform(indicatorData);
//		
//		indicatorData = new StringIndexer()
//				.setInputCol("formId")
//				.setOutputCol("form_index")
//				.fit(indicatorData)
//				.transform(indicatorData);
//		
//		indicatorData = new StringIndexer()
//				.setInputCol("indicatorName")
//				.setOutputCol("indicator_index")
//				.fit(indicatorData)
//				.transform(indicatorData);
//		
//		indicatorData = new StringIndexer()
//				.setInputCol("numerator")
//				.setOutputCol("numerator_index")
//				.fit(indicatorData)
//				.transform(indicatorData);
//		
//		indicatorData.show();
//		System.out.println("after string indexing*********************************");
//		
//		/*
//		 * This process is used to minimize the number of columns by encoding 
//		 * multiple categorical columns into a single vector.
//		 * For example with 5 categories, an input value of 2.0 would map to an output vector of [0.0, 0.0, 1.0, 0.0]
//		 * */
//		OneHotEncoderEstimator encoder = new OneHotEncoderEstimator();
//		indicatorData = encoder.setInputCols(new String[] {"indicator_index", "form_index",  "subgroup_index", "numerator_index"})
//								.setOutputCols(new String[] {"indicator_vector", "form_vector",  "subgroup_vector", "numerator_vector"})
//								.fit(indicatorData)
//								.transform(indicatorData);
//		
//		//Extracting features from the specified columns
//		VectorAssembler vectorAssembler = new VectorAssembler();
//		Dataset<Row> inputData = vectorAssembler.setInputCols(new String[] {"indicator_vector", "numerator_vector"})
//				.setOutputCol("features")
//				.transform(indicatorData)
//				.select("indicatorNid", "indicatorName", "features");
//		
//		inputData.show();
//		
//		//Applying K-means clustering 
//		KMeans kmeans = new KMeans();
//			kmeans.setK(97);
//			KMeansModel kMeansModel = kmeans.fit(inputData);
//			Dataset<Row> predictions = kMeansModel.transform(inputData);
////			predictions.show();
////			predictions.select("indicatorNid", "indicatorName", "prediction").write().format("com.databricks.spark.csv").option("header", "true").save("E://indicator_cluster3");
//			List<RelatedIndicators> relatedIndicators = new ArrayList<RelatedIndicators>();
//			predictions.select("indicatorNid", "prediction").collectAsList().forEach(f -> {
//				RelatedIndicators rl = new RelatedIndicators();
//				rl.setIndicatorId(f.getAs(0));
//				rl.setGroupId(f.getAs(1));
//				relatedIndicatorsRepository.save(rl);
//				relatedIndicators.add(rl);
//			});
//			
////			predictions.groupBy("prediction").count().show();
//			System.out.println("SSE is " + kMeansModel.computeCost(inputData));
//			
//			ClusteringEvaluator evaluator = new ClusteringEvaluator();
//			System.out.println("Slihouette with squared euclidean distance is " + evaluator.evaluate(predictions));
//			System.out.println("*******************************************************************");
//			
//			spark.close();
//	}
//	
//	/*
//	 * author : Biswabhusan Pradhan
//	 * email : biswabhusan@sdrc.co.in
//	 */
//	public void trainClusterWithTokenizedWord() {
//		System.setProperty("hadoop.home.dir", "C:\\Program Files\\Hadoop");
//		Logger.getLogger("org.apache").setLevel(Level.ERROR);
//		
//		//Creating the spark session and configuring
//		SparkSession spark = SparkSession.builder()
//				.appName("Indicator Clustering")
//				.config("spark.warehouse.dir", "file:///c:/tmp/")
//				.master("local[*]")
//				.getOrCreate();
//		
//		//Loading the dataset
//		Dataset<Row> indicatorData = spark.read()
//				.option("header", true)
//				.option("inferSchema", true)
//				.csv("E:\\\\rani-workspace\\\\indicator1.csv");
//		indicatorData.show();
//		
//		indicatorData = indicatorData.select("indicatorNid", "indicatorName", "subgroup")
//				.where(col("subgroup").isNotNull())
//				.withColumn("indicatorName", lower(col("indicatorName")))
//				.withColumn("subgroup", lower(col("subgroup")));
//		
//		/*
//		 * Tokenizing the indicators into words/tokens.Tokenization is the process of 
//		 * taking text (such as a sentence) and breaking it into individual terms (usually words)
//		 * */
//		Tokenizer indicatorTokenizer = new Tokenizer()
//				.setInputCol("indicatorName")
//				.setOutputCol("indicator_token");
//		Dataset<Row> tokenizedData= indicatorTokenizer.transform(indicatorData);
//		tokenizedData.show();
//		
//		//Defining the stopwords
//		String[] stopwords = new String[] {"of", "the", "is", "are", "for", "with", "on",  
//				"where", "were", "which", "to", "their", "was", "there", "persons", "total", "workers", "percentage"};
//		
//		//Filtering the stopwords
//		StopWordsRemover indicatorFilter = new StopWordsRemover()
//				.setStopWords(stopwords)
//				.setInputCol("indicator_token")
//				.setOutputCol("filtered_indicator");
//		Dataset<Row> filteredData = indicatorFilter.transform(tokenizedData);
//		filteredData.show();
//		
//		//Defining the the number of tokens we will consider during the clustering process
//		NGram ngramTransformer = new NGram()
//				.setN(2)
//				.setInputCol("filtered_indicator")
//				.setOutputCol("indicator_ngrams");
//		
//		Dataset<Row> ngramData = ngramTransformer.transform(filteredData);
//		ngramData.show();
//		
//		indicatorData = ngramData.selectExpr("indicatorNid", "indicatorName", "indicator_ngrams[0]","subgroup");
//		indicatorData.show();
//		
//		//String indexing the text fields into numeric forms
//		StringIndexer indicatorIndexer = new StringIndexer()
//				.setInputCol("indicator_ngrams[0]")
//				.setOutputCol("indicator_index").setHandleInvalid("skip");
//		indicatorData = indicatorIndexer.fit(indicatorData)
//				.transform(indicatorData);
//		
//		indicatorData = new StringIndexer()
//				.setInputCol("subgroup")
//				.setOutputCol("subgroup_index").setHandleInvalid("skip")
//				.fit(indicatorData)
//				.transform(indicatorData);
//		
//		indicatorData.show();
//		System.out.println("after string indexing*********************************");
//		
//		/*
//		 * This process is used to minimize the number of columns by encoding 
//		 * multiple categorical columns into a single vector.
//		 * For example with 5 categories, an input value of 2.0 would map to an output vector of [0.0, 0.0, 1.0, 0.0]
//		 * */
//		OneHotEncoderEstimator encoder = new OneHotEncoderEstimator();
//		indicatorData = encoder.setInputCols(new String[] {"indicator_index"})
//							.setOutputCols(new String[] {"indicator_vector"})
//							.fit(indicatorData)
//							.transform(indicatorData);
//		
//		//Extracting features from the specified columns
//		VectorAssembler vectorAssembler = new VectorAssembler();
//		Dataset<Row> inputData = vectorAssembler.setInputCols(new String[] {"indicator_vector"})
//				.setOutputCol("features")
//				.transform(indicatorData)
//				.select("indicatorNid", "indicator_ngrams[0]", "indicatorName", "features");
//		
//		inputData.show();
//		
//		//Applying K-means clustering 
//		KMeans kmeans = new KMeans();
////		for (int i = 1200; i <= 1205; i++) {
//			kmeans.setK(50);
////			System.out.println("k = "+i);
//			KMeansModel kMeansModel = kmeans.fit(inputData);
//			Dataset<Row> predictions = kMeansModel.transform(inputData);
////			predictions.show();
//			predictions.select("indicatorName", "indicator_ngrams[0]", "prediction").write().format("com.databricks.spark.csv")
//			.option("header", "true").save("E://census/indicator_cluster_");
//			predictions.groupBy("prediction").count().show();
//			List<RelatedIndicators> relatedIndicators = new ArrayList<RelatedIndicators>();
//			predictions.select("indicatorNid", "prediction").collectAsList().forEach(f -> {
//				RelatedIndicators rl = new RelatedIndicators();
//				rl.setIndicatorId(f.getAs(0));
//				rl.setGroupId(f.getAs(1));
//				relatedIndicatorsRepository.save(rl);
//				relatedIndicators.add(rl);
//			});
//			System.out.println("SSE is " + kMeansModel.computeCost(inputData));
//			
//			ClusteringEvaluator evaluator = new ClusteringEvaluator();
//			System.out.println("Slihouette with squared euclidean distance is " + evaluator.evaluate(predictions));
//			System.out.println("*******************************************************************");
////		}
//			spark.close();
//	}
//
//	public List<Integer> getRelatedIndicators(Integer indicatorId) {
//		// TODO Auto-generated method stub
//		RelatedIndicators selectedIndicator = relatedIndicatorsRepository.findTop1ByIndicatorId(indicatorId);
//		List<RelatedIndicators> _rl = relatedIndicatorsRepository.findTop5ByGroupId(selectedIndicator!=null ? selectedIndicator.getGroupId() : 0);
//		List<Integer> _rlidlist = _rl.stream().map(f -> f.getIndicatorId()).collect(Collectors.toList());
//		return _rlidlist;
//		}
//	}
