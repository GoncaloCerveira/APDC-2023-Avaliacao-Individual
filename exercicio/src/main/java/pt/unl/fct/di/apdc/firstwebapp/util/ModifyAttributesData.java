package pt.unl.fct.di.apdc.firstwebapp.util;

import com.google.cloud.datastore.Entity;

public class ModifyAttributesData {

	public String user; //User that modifies

	public String username;
	public String email;
	public String name;
	public String profile;
	public String telephone;
	public String mobilePhone;
	public String profession;
	public String workPlace;
	public String address;
	public String additionalAddress;
	public String city;
	public String zip;
	public String nif;
	public String role;
	public String state;

	public ModifyAttributesData() {}
	
	public ModifyAttributesData(String user, String username, String email, String name, String profile,
									String homePhone, String mobilePhone, String profession, String workPlace,
									String address, String additionalAdress, String city, String zip, String nif, String role, String state) {
		this.user = user;
		this.username = username;
		this.email = email;
		this.name = name;
		this.profile = profile;
		this.telephone = homePhone;
		this.mobilePhone = mobilePhone;
		this.profession = profession;
		this.workPlace = workPlace;
		this.address = address;
		this.additionalAddress = additionalAdress;
		this.city = city;
		this.zip = zip;
		this.nif = nif;
		this.role = role;
		this.state = state;
	}
	
	public void fillEmpty(Entity username) {
		if(this.email == null)
			this.email = username.getString("email");
		
		if(this.name == null) 
			this.name = username.getString("name");
		
		if(this.profile == null)
			this.profile = username.getString("profile");
		
		if(this.telephone == null)
			this.telephone = username.getString("telephone");
		
		if(this.mobilePhone == null)
			this.mobilePhone = username.getString("mobilePhone");
		
		if(this.profession == null)
			this.profession = username.getString("profession");
		
		if(this.workPlace == null) 
			this.workPlace = username.getString("workPlace");
		
		if(this.address == null)
			this.address = username.getString("address");
		
		if(this.additionalAddress == null) 
			this.additionalAddress = username.getString("additionalAddress");
		
		if(this.city == null) 
			this.city = username.getString("city");
		
		if(this.zip == null) 
			this.zip = username.getString("zip");
		
		if(this.nif == null)
			this.nif = username.getString("nif");
		
		if(this.role == null)
			this.role = username.getString("role");
		
		if(this.state == null)
			this.state = username.getString("state");
	}
}