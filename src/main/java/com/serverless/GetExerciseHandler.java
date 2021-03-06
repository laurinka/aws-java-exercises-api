package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.serverless.dal.Exercise;
import org.apache.log4j.Logger;

import java.util.Map;

public class GetExerciseHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

    try {
        // get the 'pathParameters' from input
        Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
        String exerciseId = pathParameters.get("id");

        // get the Exercise by id
        Exercise exercise = new Exercise().get(exerciseId);

        // send the response back
        if (exercise != null) {
          return ApiGatewayResponse.builder()
      				.setStatusCode(200)
      				.setObjectBody(exercise)
      				.build();
        } else {
          return ApiGatewayResponse.builder()
      				.setStatusCode(404)
              .setObjectBody("Exercise with id: '" + exerciseId + "' not found.")
      				.build();
        }
    } catch (Exception ex) {
        logger.error("Error in retrieving exercise: " + ex);

        // send the error response back
  			Response responseBody = new Response("Error in retrieving exercise: ", input);
  			return ApiGatewayResponse.builder()
  					.setStatusCode(500)
  					.setObjectBody(responseBody)
  					.build();
    }
	}
}