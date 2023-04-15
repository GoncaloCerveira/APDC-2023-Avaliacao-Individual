package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.datastore.*;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoginResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginUser(LoginData data) {
		LOG.fine("Attempt to login user: " + data.username);

		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key userTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);
	
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity user = txn.get(userKey);
			
			if(user == null) {
				txn.rollback();
				LOG.warning("Login operation canceled, " + data.username + " does not exist.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " does not exist.").build();
			}
			else {
				String password = user.getString("password");
				if(!password.equals(DigestUtils.sha512Hex(data.password))) {
					txn.rollback();
					LOG.warning("Incorrect password for user " + data.username);

					return Response.status(Status.BAD_REQUEST).entity("Incorrect password.").build();
				}
				else {
					AuthToken at = new AuthToken(data.username, user.getString("role"), user.getString("state")); 
					Entity userToken = txn.get(userTokenKey);
				
					if(userToken != null && isTokenValid(userToken)) {
						txn.rollback();
						LOG.warning("Login operation canceled, " + data.username + " is already logged in.");

						return Response.status(Status.BAD_REQUEST).entity(data.username + " is already logged in.").build();
					}
					
					userToken = Entity.newBuilder(userTokenKey)		//Create a new Token to be used while Logged In
							.set("username", at.username)
							.set("tokenId", at.tokenID)
							.set("creationData", at.creationData)
							.set("expirationData", at.expirationData)
							.set("role", at.role)
							.set("state", at.state)
							.build();
					
					txn.put(userToken);
					LOG.info("User " + data.username + " has successfuly logged in.");
					txn.commit();

					return Response.ok(g.toJson(at)).build();
				}
			}
		}
		finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
	
	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();
		return !(token.getLong("expirationData") < currentTime);
	}
}
