package pt.unl.fct.di.apdc.firstwebapp.util;

public class RemoveData {
	
	public RemoveData() { }
	
	public String removedUser; 	//User that is going to be removed
	
	public String user; 	//User that is removing the user
	
	public RemoveData(String removedUser, String user) {
		this.removedUser = removedUser;
		this.user = user;
	}
	
	public boolean isValid() {
		if(this.removedUser == null || this.user == null ||
				this.removedUser.length() == 0 || this.user.length() == 0)
			return false;
		return true;
	}
}
