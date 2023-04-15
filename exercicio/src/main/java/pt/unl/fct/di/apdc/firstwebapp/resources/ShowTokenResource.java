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
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.ShowTokenData;

@Path("/showToken")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowTokenResource {
	
	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());

	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ShowTokenResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response showToken(ShowTokenData data) {
		LOG.fine("Attempt to show the token of " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = datastore.get(userKey);

		Key userTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);
		Entity userToken = datastore.get(userTokenKey);
		
		if(user == null) {
			LOG.warning("ShowToken operation canceled, " + data.username + " does not exist.");
			return Response.status(Status.BAD_REQUEST).entity(data.username + " does not exist.").build();
		}
		
		if(userToken == null) {
			LOG.warning("ShowToken operation canceled, " + data.username + " is not logged in.");
			return Response.status(Status.BAD_REQUEST).entity(data.username + " is not logged in.").build();
		}
		
		if(!isTokenValid(userToken)) {
			LogoutResource invalidTokenAux = new LogoutResource();
			invalidTokenAux.logout(data.username);

			LOG.warning("ShowToken operation canceled, " + data.username + " session has expired.");

			return Response.status(Status.BAD_REQUEST).entity(data.username + " session has expired.").build();
		}
		
		AuthToken at = new AuthToken(userToken.getString("username"), userToken.getString("role"), userToken.getString("state"), userToken.getString("tokenId"),
				userToken.getLong("creationData"), userToken.getLong("expirationData"));

		return Response.ok(g.toJson(at)).build();
	}
	
	private boolean isTokenValid(Entity token) {
		long currentTime = System.currentTimeMillis();
		return !(token.getLong("expirationData") < currentTime);
	}
}
