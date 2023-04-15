package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

	//Mandatory
	public String username;
	public String email;
	public String name;
	public String password;
	public String confirmation;
	
	//Optional
	public String profile;
	public String telephone;
	public String mobilePhone;
	public String profession;
	public String workPlace;
	//Address
	public String address;
	public String additionalAddress;
	public String city;
	public String zip;
	public String nif;
	
	private static final String EMPTY = "Not Filled";
	
	public RegisterData() {}
	
	public RegisterData(String username, String email, String name, String password, String confirmation,
						String profile, String telephone, String mobilePhone, String profession, String workPlace, 
						String address, String additionalAdress, String city, String zip, String nif) {
		this.username = username;
		this.email = email;
		this.name = name;
		this.password = password;
		this.confirmation = confirmation;
		this.profile = profile;
		this.telephone = telephone;
		this.mobilePhone = mobilePhone;
		this.profession = profession;
		this.workPlace = workPlace;
		this.address = address;
		this.additionalAddress = additionalAdress;
		this.city = city;
		this.zip = zip;
		this.nif = nif;
	}

	public void fillOptional() {
		if(this.profile == null)
			this.profile = EMPTY;
		
		if(this.telephone == null)
			this.telephone = EMPTY;
		
		if(this.mobilePhone == null)
			this.mobilePhone = EMPTY;
		
		if(this.address == null) 
			this.address = EMPTY;
		
		if(this.additionalAddress == null) 
			this.additionalAddress = EMPTY;
		
		if(this.city == null) 
			this.city = EMPTY;
		
		if(this.zip == null) 
			this.zip = EMPTY;

		if(this.profession == null)
			this.profession = EMPTY;
		
		if(this.workPlace == null) 
			this.workPlace = EMPTY;
		
		if(this.nif == null) 
			this.nif = EMPTY;
	}
	
	public int isValid() {
		if(this.username == null || this.password == null || this.confirmation == null 
				|| this.email == null || this.name == null || this.username.length() == 0 
				|| this.email.length() == 0 || this.name.length() == 0
				|| this.password.length() == 0 || this.confirmation.length() == 0)
			return 0;

		if(!this.password.equals(this.confirmation))
			return 1;
		
		return 2;
	}
	
	public boolean checkPass() {
		char[] pass = this.password.toCharArray();
		int checkDigit = 0, checkUpperCase = 0;
		
		for(char i: pass) {
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
