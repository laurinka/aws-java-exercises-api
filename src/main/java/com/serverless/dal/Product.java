package com.serverless.dal;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

@DynamoDBTable(tableName = "PLACEHOLDER_EXERCISES_TABLE_NAME")
public class Product {

    // get the table name from env. var. set in serverless.yml
    private static final String EXERCISES_TABLE_NAME = System.getenv("EXERCISES_TABLE_NAME");

    private static DynamoDBAdapter db_adapter;
    private final AmazonDynamoDB client;
    private final DynamoDBMapper mapper;

    private Logger logger = Logger.getLogger(this.getClass());

    private String id;
    private String name;
    private Float price;

    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @DynamoDBRangeKey(attributeName = "name")
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "price")
    public Float getPrice() {
        return this.price;
    }
    public void setPrice(Float price) {
        this.price = price;
    }

    public Product() {
        // build the mapper config
        DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
            .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(EXERCISES_TABLE_NAME))
            .build();
        // get the db adapter
        this.db_adapter = DynamoDBAdapter.getInstance();
        this.client = this.db_adapter.getDbClient();
        // create the mapper with config
        this.mapper = this.db_adapter.createDbMapper(mapperConfig);
    }

    public String toString() {
        return String.format("Product [id=%s, name=%s, price=$%f]", this.id, this.name, this.price);
    }

    // methods
    public Boolean ifTableExists() {
        return this.client.describeTable(EXERCISES_TABLE_NAME).getTable().getTableStatus().equals("ACTIVE");
    }

    public List<Product> list() throws IOException {
      DynamoDBScanExpression scanExp = new DynamoDBScanExpression();
      List<Product> results = this.mapper.scan(Product.class, scanExp);
      for (Product p : results) {
        logger.info("Exercises - list(): " + p.toString());
      }
      return results;
    }

    public Product get(String id) throws IOException {
        Product product = null;

        HashMap<String, AttributeValue> av = new HashMap<String, AttributeValue>();
        av.put(":v1", new AttributeValue().withS(id));

        DynamoDBQueryExpression<Product> queryExp = new DynamoDBQueryExpression<Product>()
            .withKeyConditionExpression("id = :v1")
            .withExpressionAttributeValues(av);

        PaginatedQueryList<Product> result = this.mapper.query(Product.class, queryExp);
        if (result.size() > 0) {
          product = result.get(0);
          logger.info("Exercises - get(): exercise - " + product.toString());
        } else {
          logger.info("Exercises - get(): exercise - Not Found.");
        }
        return product;
    }

    public void save(Product product) throws IOException {
        logger.info("Exercises - save(): " + product.toString());
        this.mapper.save(product);
    }

    public Boolean delete(String id) throws IOException {
        Product product = null;

        // get product if exists
        product = get(id);
        if (product != null) {
          logger.info("Exercises - delete(): " + product.toString());
          this.mapper.delete(product);
        } else {
          logger.info("Exercises - delete(): exercise - does not exist.");
          return false;
        }
        return true;
    }

}