package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement for querying for an object by its ID.
 * 
 * @author graywatson
 */
public class MappedQueryForId<T, ID> extends BaseMappedQuery<T, ID> {

	private final String label;

	protected MappedQueryForId(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			FieldType[] resultsFieldTypes, String label) {
		super(tableInfo, statement, argFieldTypes, resultsFieldTypes);
		this.label = label;
	}

	/**
	 * Query for an object in the database which matches the id argument.
	 */
	public T execute(DatabaseConnection databaseConnection, ID id) throws SQLException {
		Object[] args = new Object[] { convertIdToFieldObject(id) };
		// @SuppressWarnings("unchecked")
		Object result = databaseConnection.queryForOne(statement, args, new FieldType[] { idField }, this);
		if (result == null) {
			logger.debug("{} using '{}' and {} args, got no results", label, statement, args.length);
		} else if (result == DatabaseConnection.MORE_THAN_ONE) {
			logger.error("{} using '{}' and {} args, got >1 results", label, statement, args.length);
			logArgs(args);
			throw new SQLException(label + " got more than 1 result: " + statement);
		} else {
			logger.debug("{} using '{}' and {} args, got 1 result", label, statement, args.length);
		}
		logArgs(args);
		@SuppressWarnings("unchecked")
		T castResult = (T) result;
		return castResult;
	}

	public static <T, ID> MappedQueryForId<T, ID> build(DatabaseType databaseType, TableInfo<T, ID> tableInfo)
			throws SQLException {
		String statement = buildStatement(databaseType, tableInfo);
		return new MappedQueryForId<T, ID>(tableInfo, statement, new FieldType[] { tableInfo.getIdField() },
				tableInfo.getFieldTypes(), "query-for-id");
	}

	protected static <T, ID> String buildStatement(DatabaseType databaseType, TableInfo<T, ID> tableInfo)
			throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot query-for-id with " + tableInfo.getDataClass()
					+ " because it doesn't have an id field");
		}
		// build the select statement by hand
		StringBuilder sb = new StringBuilder();
		appendTableName(databaseType, sb, "SELECT * FROM ", tableInfo.getTableName());
		appendWhereId(databaseType, idField, sb, null);
		return sb.toString();
	}

	private void logArgs(Object[] args) {
		if (args.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("{} arguments: {}", label, (Object) args);
		}
	}
}
