package restql.core.query;

import java.util.List;

public final class QueryInterpolator {

	private QueryInterpolator() {}

	private static <T> String parseParam(T param) {
		String parsedParam;

		if(param instanceof List) {
			parsedParam = "[";

			List listParam = (List) param;

			boolean first = true;
			for(Object listValue : listParam) {
				if(first) {
					first = false;
				}
				else {
					parsedParam += ",";
				}
				parsedParam += parseParam(listValue);
			}

			parsedParam += "]";
		}
		else if (param instanceof String) {
			parsedParam = "\"" + param + "\"";
		} else {
			parsedParam = param.toString();
		}

		return parsedParam;
	}

	public static String interpolate(String query, Object... args) {
		final String queryWithPlaceHolders = query.replace("?", "%s");
		final String[] escapedArgs = new String[args.length];

		for (int i = 0; i < args.length; i++) {
			escapedArgs[i] = parseParam(args[i]);
		}


		return String.format(queryWithPlaceHolders, escapedArgs);
	}
}
