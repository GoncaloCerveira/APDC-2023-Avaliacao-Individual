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

import pt.unl.fct.di.apdc.firstwebapp.util.RemoveData;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(RemoveResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public RemoveResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response removeUser(RemoveData data) {
		LOG.fine("Attempt to remove " + data.removedUser + " by " + data.user);
		
		Key removedUserKey = datastore.newKeyFactory().setKind("User").newKey(data.removedUser);
		Key removedUserTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.removedUser);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.user);
		Key userTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.user);

		Transaction txn = datastore.newTransaction();
		
		try {
			Entity removedUser = txn.get(removedUserKey);
			Entity user = txn.get(userKey);
			
			if(user == null || removedUser == null) {
				txn.rollback();
				LOG.warning("ListUsers operation canceled, one of the users does not exist.");

				return Response.status(Status.BAD_REQUEST).entity("One of the users does not exist.").build();
			}
			
			if(!data.isValid()) {
				txn.rollback();
				LOG.warning("Remove operation canceled, one of the users does not exist.");
				
				return Response.status(Status.BAD_REQUEST).entity("One of the users does not exist.").build();
			}
			
			Entity userToken = txn.get(userTokenKey);
			
			if(userToken == null) {
				txn.rollback();
				LOG.warning("Remove operation canceled, " + data.user + " is not logged in.");

				return Response.status(Status.BAD_REQUEST).entity(data.user + " is not logged in.").build();
			}
			
			if(!isTokenValid(userToken)) {
				LogoutResource invalidTokenAux = new LogoutResource();
				invalidTokenAux.logout(data.user);

				txn.rollback();
				LOG.warning("Remove operation canceled, " + data.user + " session has expired.");

				return Response.status(Status.BAD_REQUEST).entity(data.user + " session has expired.").build();
			}
			
			if(user.getString("state").equals("INATIVO")) {
				txn.rollback();
				LOG.warning("Remove operation canceled, " + data.user + " is not active in the webapp.");

				return Response.status(Status.BAD_REQUEST).entity(data.user + " is not active in the webapp.").build();
			}
			
			if(isRemoveValid(user.getString("role"), removedUser.getString("role"), data.user, data.removedUser)) {
				txn.delete(removedUserKey);
				txn.delete(removedUserTokenKey);

				LOG.info("User " + data.removedUser + " has been successfuly removed.");
				txn.commit();

				return Response.ok("User " + data.removedUser + " has been removed.").build();
			} 
			else {
				txn.rollback();
				LOG.warning("Remove operation canceled, " + data.user + " problem with permissios by the user " + data.removedUser);

				return Response.status(Status.BAD_REQUEST).entity("Problem with permissions.").build();
			}
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}

	private boolean isRemoveValid(String userRole, String removedUserRole, String user, String removedUser) {
		if(userRole.equals("SU"))
			return true;
		
		if(userRole.equals("USER") && user.equals(removedUser))
			return true;
		
		if(userRole.equals("GBO") && (user.equals(removedUser) || userRole.equals("GBO") 
				&& removedUserRole.equals("USER")))
			return true;
		
		if(userRole.equals("GA") && (user.equals(removedUser) || userRole.equals("GA") 
				&& (removedUserRole.equals("USER") || removedUserRole.equals("GBO"))))
			return true;
		
		if(userRole.equals("GS") && (user.equals(removedUser) || userRole.equals("GS") 
				&& (removedUserRole.equals("USER") || removedUserRole.equals("GBO") || removedUserRole.equals("GA"))))
			return true;
		return false;
	}

	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();

		if(token.getLong("expirationData") < currentTime)
			return false;
		return true;
	}
}
