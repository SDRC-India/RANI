//package org.sdrc.datum19.service;
//
//import org.apache.spark.SparkConf;
//import org.apache.spark.api.java.JavaSparkContext;
//import org.apache.spark.ml.feature.StringIndexer;
//import org.apache.spark.ml.recommendation.ALS;
//import org.apache.spark.ml.recommendation.ALSModel;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import org.apache.spark.sql.SparkSession;
//import org.sdrc.datum19.document.IndicatorSuggestionModel;
//import org.sdrc.datum19.repository.SuggestionModelRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.stereotype.Service;
//
//import com.mongodb.spark.MongoSpark;
//
//
///*
// * author : Biswabhusan Pradhan
// * email : biswabhusan@sdrc.co.in
// * description : this service deals with training and storing trained data for user recommendation 
// * using collaborative filtering mechanism
// */
//@Service
//public class ModelTrainingServiceImpl implements ModelTrainingService {
//
//	@Autowired
//	private ConfigurableEnvironment configurableEnvironment;
//	
//	@Autowired
//	private SuggestionModelRepository suggestionModelRepository;
//	
//	/*
//	 * author : Biswabhusan Pradhan
//	 * email : biswabhusan@sdrc.co.in
//	 */
//	@Override
//	public void trainSearchModel() {
//		// TODO Auto-generated method stub
//
////		System.setProperty("hadoop.home.dir", "C:\\Program Files\\Hadoop");
////		Logger.getLogger("org.apache").setLevel(Level.ERROR);
//		
//		//Configuring the source DB for spark
//		SparkConf config = new SparkConf().set("spark.mongodb.input.uri",
//				configurableEnvironment.getProperty("spark.mongodb.input.db"));
//		
//		//Creating spark session
//		SparkSession spark = SparkSession.builder()
//				.appName("Indicator Recommender")
//				.config("spark.warehouse.dir", "file:///c:/tmp/")
//				.master("local[*]")
//				.config(config)
//				.getOrCreate();
//		JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
//				jsc.setLogLevel("ERROR");
//				
//		//Loading the dataset
//		Dataset<Row> csvData = MongoSpark.load(jsc).toDF();
//		csvData.show();
//		StringIndexer usernameIndexer = new StringIndexer();
//		csvData = usernameIndexer.setInputCol("username")
//			.setOutputCol("userId")
//			.fit(csvData)
//			.transform(csvData);
//		
//		csvData.groupBy("username").pivot("indicatorId").sum("count").show();
//		
//		//Creating ALS model for collaborative filtering and configuring the user column, item col and rating col
//		ALS als = new ALS()
//				.setMaxIter(10)
//				.setUserCol("userId")
//				.setItemCol("indicatorId")
//				.setRatingCol("count");
//		
//		als.setColdStartStrategy("drop");
//		
//		//Fitting the data to train model
//		ALSModel model = als.fit(csvData);
//		
////		This process will take the entire data on which it has been trained and will predict the gaps of each user
//		Dataset<Row> userRecommendations = model.recommendForAllUsers(5);
//
//		//selecting the user and recommended indicators for each user
//		userRecommendations = userRecommendations.join(csvData).where(csvData.col("userId").equalTo(userRecommendations.col("userId")))
//				.select("username", "recommendations").dropDuplicates();
//		
//		if(!suggestionModelRepository.findAll().isEmpty())
//			suggestionModelRepository.deleteAll();
//		
//		//saving the user wise recommendation results to the DB
//		userRecommendations.takeAsList(5).forEach(u -> {
//			System.out.println("User "+u.getAs(0)+", we suggest you to see "+u.getAs(1).toString()+" indicators");
//			String[] a = u.getAs(1).toString().split("\\[");
//			for (int i=1; i<a.length; i++) {
//				IndicatorSuggestionModel suggestionModel = new IndicatorSuggestionModel();
//				String[] b =a[i].split("\\]")[0].split(",");
//				suggestionModel.setUsername(u.getAs(0));
//				suggestionModel.setIndicatorId(Integer.parseInt(b[0]));
//				suggestionModel.setPrediction(Double.parseDouble(b[1]));
//				
//				suggestionModelRepository.save(suggestionModel);
//			}
//		});
//		
//		//Item wise recommendation
//		Dataset<Row> indicatorRecommendations = model.recommendForAllItems(5);
//		indicatorRecommendations.takeAsList(5).forEach(indicator -> {
//			System.out.println("Users who might see Indicator-"+indicator.getAs(0)+" are "+indicator.getAs(1).toString());
//		});
//		jsc.close();
//	}
//
//}
