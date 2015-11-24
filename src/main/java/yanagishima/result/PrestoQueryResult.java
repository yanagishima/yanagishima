package yanagishima.result;

import java.util.List;

public class PrestoQueryResult {
	
	private String updateType;
	
	private List<String> columns;
	
	private List<List<Object>> records;
	
	private String warningMessage;

	private String queryId;

	public String getUpdateType() {
		return updateType;
	}

	public void setUpdateType(String updateType) {
		this.updateType = updateType;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public List<List<Object>> getRecords() {
		return records;
	}

	public void setRecords(List<List<Object>> records) {
		this.records = records;
	}

	public String getWarningMessage() {
		return warningMessage;
	}

	public void setWarningMessage(String warningMessage) {
		this.warningMessage = warningMessage;
	}

	public String getQueryId() { return queryId; }

	public void setQueryId(String queryId) { this.queryId = queryId; }

}
