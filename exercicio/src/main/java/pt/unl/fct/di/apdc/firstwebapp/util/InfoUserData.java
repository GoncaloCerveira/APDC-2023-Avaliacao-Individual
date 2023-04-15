package pt.unl.fct.di.apdc.firstwebapp.util;

public class InfoUserData {

	//Mandatory
	public String username;
	public String email;
	public String name;
		
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
	
	public String role;
	public String state;
	
	public InfoUserData() {}
	
	public InfoUserData(String username, String email, String name,
			String profile, String telephone, String mobilePhone, String profession, String workPlace, 
			String address, String additionalAdress, String city, String zip, String nif, String role, String state) {
		this.username = username;
		this.email = email;
		this.name = name;
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
		this.role = role;
		this.state = state;
	}
}
