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

import pt.unl.fct.di.apdc.firstwebapp.util.LogoutData;


@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {
	
	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LogoutResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response logoutUser(LogoutData data) {
		LOG.fine("Attempt to logout user: " + data.username);
		
		Key userTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);
	
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity userToken = txn.get(userTokenKey);

			if(userToken == null) {
				txn.rollback();
				LOG.warning("Logout operation canceled, " + data.username + " is not logged in or does not exist.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " is not logged in.").build();
			}

			txn.delete(userTokenKey);

			LOG.info(data.username + " has successfuly logged out.");

			txn.commit();

			return Response.ok(Status.BAD_REQUEST).entity(data.username + " has successfuly logged out.").build();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
	
	public void logout(String username) {	//To logout if token invalid/expired
		LogoutData data = new LogoutData(username);
		logoutUser(data);
	}
}
