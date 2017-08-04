package yanagishima.result;

import io.airlift.units.DataSize;

import java.util.List;

public class HiveQueryResult {

    private List<String> columns;

    private List<List<String>> records;

    private String warningMessage;

    private String queryId;

    private int lineNumber;

    private DataSize rawDataSize;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<String>> getRecords() {
        return records;
    }

    public void setRecords(List<List<String>> records) {
        this.records = records;
    }

    public String getWarningMessage() {
        return warningMessage;
    }

    public void setWarningMessage(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public DataSize getRawDataSize() {
        return rawDataSize;
    }

    public void setRawDataSize(DataSize rawDataSize) {
        this.rawDataSize = rawDataSize;
    }

}
