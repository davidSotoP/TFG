package exportador.configuration;

public enum ExportadorRoles {
	
	ADMIN("A", Roles.ROLE_ADMIN);
	
	private String dbCode;
	private String name;
	
	ExportadorRoles(String name, String dbCode) {
		this.name = name;
		this.dbCode = dbCode;
	}

	public String getDbCode() {
		return dbCode;
	}

	public void setDbCode(String dbCode) {
		this.dbCode = dbCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static class Roles {
		private Roles() {
			
		}
		
		public static final String ROLE_ADMIN = "ROLE_ADMIN";

		public static String getRoleAdmin() {
			return ROLE_ADMIN;
		}
	}
}
