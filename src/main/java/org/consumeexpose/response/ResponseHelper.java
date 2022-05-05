package org.consumeexpose.response;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.consumeexpose.MemoryHeap;
import org.consumeexpose.annotations.Null;
import org.consumeexpose.annotations.ResponseCode;
import org.consumeexpose.annotations.ResponsePolicy;
import org.consumeexpose.annotations.VoidType;
import org.consumeexpose.util.ConstructorInterpretor;
import org.json.JSONObject;

public class ResponseHelper {

	private static MemoryHeap heap = MemoryHeap.getInstance();

	public static final String ALL = "ALL";
	public static final String VOID = "VOID";
	public static final String NULL = "NULL";

	public static final int INTERNAL_SERVER_ERROR = 500;

	public static final int SUCCESS = 200;

	public static final int VOID_RESPONSE_CODE_GET = 202;

	public static final int VOID_RESPONSE_CODE_POST = 201;

	public static final int VOID_RESPONSE_CODE_DELETE = 202;

	public static final int NULL_RESPONSE_CODE = 404;

	private static final String EXCEPTION_ERROR_REGX = "([0-9]{3})-(.*)";

	private static final String TYPE = "type";
	private static final String VALUE = "value";
	private static final String ERROR = "error";
	private static final String ERROR_MESSAGE = "Requested Resource is not Found/computed";
	private static final String SERVER_ERROR = "Internal Server error";

	public static void determineResponsePolicies() {

		String responseForClass = null;

		for (Class<?> classDef : heap.responsePolicyClasses) {
			responseForClass = classDef.getAnnotation(ResponsePolicy.class).classDef();

			if (responseForClass.equals("")) {

				if (heap.responsePolicies.get(ALL) != null) {
					System.out.println("[echo]:A generic response policy had already been defined, replacing it with:"
							+ classDef.getName());

					heap.responsePolicies.put(ALL, getResponseCodeDefs(classDef));
				} else
					heap.responsePolicies.put(ALL, getResponseCodeDefs(classDef));
			} else if (heap.responsePolicies.get(classDef.getName()) != null) {
				System.out.println(
						"[echo]:A response policy had already been defined for the same class, replacing it with:"
								+ classDef.getName());

				heap.responsePolicies.put(classDef.getName(), getResponseCodeDefs(classDef));
			} else
				heap.responsePolicies.put(classDef.getName(), getResponseCodeDefs(classDef));

		}

	}

	private static HashMap<String, Integer> getResponseCodeDefs(Class<?> classDef) {
		HashMap<String, Integer> responseCodeDefs = new HashMap<String, Integer>();
		Field[] fields = classDef.getFields();
		Object instance = null;
		Class<?>[] classTypes = {};
		Object[] values = {};
		try {
			instance = classDef.getDeclaredConstructor(classTypes).newInstance(values);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			System.out.println("[echo]:Error while fetching the default constructor");

		}
		for (Field field : fields) {

			try {
				if (field.isAnnotationPresent(Null.class))
					responseCodeDefs.put(NULL, field.getInt(instance));
				else if (field.isAnnotationPresent(VoidType.class))
					responseCodeDefs.put(VOID, field.getInt(instance));
				else if (field.isAnnotationPresent(ResponseCode.class))
					responseCodeDefs.put(field.getType().getName(), field.getAnnotation(ResponseCode.class).value());

			} catch (IllegalArgumentException | IllegalAccessException e) {
				System.out.println("[echo]:Error in retrieving the value of Field:" + field.getName());
			}

		}

		return responseCodeDefs;
	}

	public static Response getExceptionResponse(String message) {
		if (message == null)
			return null;
		Pattern pattern = Pattern.compile(EXCEPTION_ERROR_REGX);
		Matcher matcher = pattern.matcher(message);
		if (matcher.matches()) {
			return Response.setStatus(Integer.parseInt(matcher.group(1))).setResponseBody(matcher.group(2));
		} else
			return null;
	}

	public static Response[] getPossiblePrimitiveResponses(Class<?> classDef,HashMap<String, Integer> responsePolicy) {

		JSONObject response = new JSONObject();
		Response[] responsesList = new Response[3];
		Response responseObject;
		int count=0;
		int statusCode = 0;

		if (responsePolicy.get(NULL) != null) {
			statusCode = responsePolicy.get(NULL);
			response.put(ERROR,ERROR_MESSAGE);

		} else {
			statusCode = NULL_RESPONSE_CODE;
			response.put(ERROR,ERROR_MESSAGE);
		}
		responseObject = new Response(statusCode,response.toString());
		responsesList[count] = responseObject;
		count++;
		response = new JSONObject();
		response.put(TYPE, classDef.getSimpleName());
		response.put(VALUE, "");
		statusCode = SUCCESS;
		responseObject = new Response(statusCode,response.toString());
		responsesList[count] = responseObject;
		count++;
		response = new JSONObject();
		statusCode = INTERNAL_SERVER_ERROR;
		response.put(ERROR,SERVER_ERROR);
		responseObject = new Response(statusCode,response.toString());
		responsesList[count] = responseObject;
		count++;
		
		
		return responsesList;	
	}
	
	public static Response[] getPossibleVoidResponses(HashMap<String, Integer> responsePolicy) {

		JSONObject response = new JSONObject();
		Response[] responsesList = new Response[2];
		int count = 0;
		Response responseObject;
		
		int statusCode = 0;

		statusCode = SUCCESS;
		responseObject = new Response(statusCode,response.toString(1));
		responsesList[count] = responseObject;
		count++;
		response = new JSONObject();
		statusCode = INTERNAL_SERVER_ERROR;
		response.put(ERROR,SERVER_ERROR);
		responseObject = new Response(statusCode,response.toString(1));
		responsesList[count] = responseObject;
		count++;
		
		return (Response[]) responsesList;		
	}
	
	public static Response[] getPossibleNonPrimitiveResponses(Class<?> classDef,HashMap<String, Integer> responsePolicy) {

		JSONObject response = new JSONObject();
		Response[] responsesList = new Response[3];
		int count = 0;
		Response responseObject;
		
		int statusCode = 0;

		if (responsePolicy.get(NULL) != null) {
			statusCode = responsePolicy.get(NULL);
			response.put(ERROR,ERROR_MESSAGE);

		} else {
			statusCode = NULL_RESPONSE_CODE;
			response.put(ERROR,ERROR_MESSAGE);
		}
		responseObject = new Response(statusCode,response.toString(1));
		responsesList[count] = responseObject;
		count++;
		response = new JSONObject();
		JSONObject payload = ConstructorInterpretor.getConstructorPayloadFromClass(classDef);
		if(payload!=null) {
			String key = payload.keys().next();
			response.put(key, payload.getJSONObject(key));
		}
		statusCode = SUCCESS;
		responseObject = new Response(statusCode,response.toString(1));
		responsesList[count] = responseObject;
		count++;
		
		response = new JSONObject();
		statusCode = INTERNAL_SERVER_ERROR;
		response.put(ERROR,SERVER_ERROR);
		responseObject = new Response(statusCode,response.toString(1));
		responsesList[count] = responseObject;
		count++;
		
		
		return (Response[]) responsesList;		
	}
	
	
	

}