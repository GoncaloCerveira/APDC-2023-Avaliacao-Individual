package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;

import pt.unl.fct.di.apdc.firstwebapp.util.ModifyAttributesData;

@Path("/modifyAttributes")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyAttributesResource {
	
	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(RemoveResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ModifyAttributesResource() {}

	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response modifyAttributes(ModifyAttributesData data) {
		LOG.fine("Attempt to modify a attribute by " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.user);			//User making changes
		Key usernameKey = datastore.newKeyFactory().setKind("User").newKey(data.username); 	//User that is going to have changes
		Key userTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.user);

		Transaction txn = datastore.newTransaction();
		
		try {
			Entity user = txn.get(userKey);
			Entity username = txn.get(usernameKey);
			
			if(user == null || username == null) {
				txn.rollback();
				LOG.warning("ModifyAttributes operation canceled, one of the users does not exist.");

				return Response.status(Status.BAD_REQUEST).entity("One of the users does not exist.").build();
			}
			
			Entity userToken = txn.get(userTokenKey);
			
			if(userToken == null) {
				txn.rollback();
				LOG.warning("ModifyAttributes operation canceled, " + data.user + " is not logged in.");

				return Response.status(Status.BAD_REQUEST).entity(data.user + " is not logged in.").build();
			}
			
			if(!isTokenValid(userToken)) {
				LogoutResource invalidTokenAux = new LogoutResource();
				invalidTokenAux.logout(data.username);

				txn.rollback();
				LOG.warning("ModifyAttributes operation canceled, " + data.user + " session has expired.");

				return Response.status(Status.BAD_REQUEST).entity(data.user + " session has expired.").build();
			}
			
			if (isModifyAttributesValid(user.getString("role"), username.getString("role"), data.user, data.username)) {
				switch (user.getString("role")) {
					case "USER":
						if(data.email != null || data.name != null || data.role != null || data.state != null) {
							txn.rollback();
							LOG.warning("ModifyAttributes operation canceled, " 
									+ data.user + " does not have the permissions to modify.");

							return Response.status(Status.BAD_REQUEST).entity(data.user + " does not have the permissions to modify.").build();
						}
						break;
					case "GBO":
						if(!username.getString("role").equals("USER") && !(data.user.equals(data.username))) {
							txn.rollback();
							LOG.warning("ModifyAttributes operation canceled, " 
									+ data.user + " does not have the permissions to modify.");

							return Response.status(Status.BAD_REQUEST).entity(data.user + " does not have the permissions to modify.").build();
						}
						break;
					case "GA":
						if ((!(data.role.equals("GBO")) || !(data.role.equals("USER")) && 
								!(data.user.equals(data.username)))) {
							txn.rollback();
							LOG.warning("ModifyAttributes operation canceled, " 
									+ data.user + " does not have the permissions to modify.");

							return Response.status(Status.BAD_REQUEST).entity(data.user + " does not have the permissions to modify.").build();
						}
						break;
					case "GS":
						if (((data.role.equals("GS") || data.role.equals("SU"))) &&
								!(data.user.equals(data.username))) {
							txn.rollback();
							LOG.warning("ModifyAttributes operation canceled, " 
									+ data.user + " does not have the permissions to modify.");

							return Response.status(Status.BAD_REQUEST).entity(data.user + " does not have the permissions to modify.").build();
						}
						break;
					default:
						break;
				}
				
				data.fillEmpty(username);
				
				username = Entity.newBuilder(usernameKey)
						.set("username", username.getString("username"))
						.set("password", username.getString("password"))
						.set("email", data.email)
						.set("name", data.name)
						.set("profile", data.profile)
						.set("telephone", data.telephone)
						.set("mobilePhone", data.mobilePhone)
						.set("profession", data.profession)
						.set("workPlace", data.workPlace)
						.set("address", data.address)
						.set("additionalAddress", data.additionalAddress)
						.set("city", data.city)
						.set("zip", data.zip)
						.set("nif", data.nif)
						.set("userCreationTime", user.getTimestamp("userCreationTime"))
						.set("role", data.role)
						.set("state", data.state)
						.build();

				txn.put(username);
				LOG.info("Attributes of " + data.username + " modifies successfuly.");
				txn.commit();

				return Response.ok("Attributes were successfuly modified.").build();
			} 
			else {
				txn.rollback();
				LOG.warning("The attributes were not possible to be changed, some of the criterias were not met.");

				return Response.status(Status.BAD_REQUEST).entity("The attributes were not possible to be changed, some of the criterias were not met.").build();
			}
		}
		finally {
			if(txn.isActive()) {
				txn.rollback();
			}
		}
	}
	
	private boolean isModifyAttributesValid(String userRole, String usernameRole, String user, String username) {
		if(user.equals(username))
			return true;
		
		if(userRole.equals("SU"))
			return true;
		
		if(userRole.equals("USER") && user.equals(username))
			return true;
		
		if(userRole.equals("GBO") && usernameRole.equals("USER"))
			return true;
		
		if(userRole.equals("GA") && (usernameRole.equals("GBO") || usernameRole.equals("USER")))
			return true;
		
		LOG.warning(userRole + " + " + usernameRole);
		if(userRole.equals("GS") && (usernameRole.equals("GBO") || usernameRole.equals("GA")))
			return true;
		return false;
	}
	
	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();
		return !(token.getLong("expirationData") < currentTime);
	}
}
