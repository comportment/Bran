package br.com.brjdevs.steven.bran.core.operations;

public enum ResultType {
	FAILURE,
	INVALID,
	SUCCESS,
	NONE;
	
	public OperationResult setExtras(Object... extra) {
		return new OperationResult(this, extra);
	}
	
	public static class OperationResult {
		private ResultType operationResult;
		private Object[] extras;
		
		private OperationResult(ResultType operationResult, Object... extras) {
			this.operationResult = operationResult;
			this.extras = extras;
		}
		
		public Object[] getExtras() {
			return extras;
		}
		
		public ResultType getResult() {
			return operationResult;
		}
	}
}