package pt.unl.fct.di.apdc.firstwebapp.util;

public class ModifyPassData {

	public String username;
	public String password;
	public String newPass;
	public String newConfirmation;
	
	public ModifyPassData() {}
	
	public ModifyPassData(String username, String password, String newPass, String newConfirmation) {
		this.username = username;
		this.password = password;
		this.newPass = newPass;
		this.newConfirmation = newConfirmation;
	}
	
	public int isValid() {
		if(this.newPass == null || this.newPass.length() == 0)
			return 0;
		if(!this.newPass.equals(this.newConfirmation))
			return 1;
		return 2;
	}
	
	
	public boolean checkPass() {
		char[] newPass = this.newPass.toCharArray();
		int checkDigit = 0, checkUpperCase = 0;
		
		for(char i: newPass) {
			if(Character.isDigit(i))
				checkDigit++;
			if(Character.isUpperCase(i))
				checkUpperCase++;
		}
		
		if(checkDigit == 0 || checkUpperCase == 0)
			return false;
		return true;
	}
}
