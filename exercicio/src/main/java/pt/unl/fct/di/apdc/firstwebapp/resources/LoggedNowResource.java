package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.LoggedNowData;

@Path("/loggedNow")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoggedNowResource {

	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public LoggedNowResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listUsers(LoggedNowData data) {
		LOG.fine("Attempt to list all the users logged in by " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity user = txn.get(userKey);
			
			if(user == null) {
				txn.rollback();
				LOG.warning("LoggedNow operation canceled, " + data.username + " does not exist.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " does not exist.").build();
			}
			
			List<String> users = new ArrayList<String>();
			String userRole = user.getString("role");
			
			switch(userRole) {
				case "SU":
					
					Query<Entity> queryLogged = Query.newEntityQueryBuilder().setKind("Token")
					   .setOrderBy(OrderBy.desc("creationData"))
					   .build();

					QueryResults<Entity> queryLoggedList = datastore.run(queryLogged);
					
					users.add("---------------------------------------------------,");
					queryLoggedList.forEachRemaining(usersList -> {
						users.add(" Username: " + usersList.getString("username") + ",");
						users.add(" Role: " + usersList.getString("role") + ",");
						users.add(" State: " + usersList.getString("state") + ",");
						Date res = new Date(usersList.getLong("creationData"));
						users.add("Logged in: " + res);
						users.add(",---------------------------------------------------" + ",,");
					});
					break;
				default:
					LOG.warning("LoggedNow operation canceled, " + data.username + " does not have the permission for this operation.");
					break;
			}
			
			users.add("Users online " + (users.size()/5));
			users.add(",---------------------------------------------------");
			LOG.info("Listing all logged in users by " + data.username + " was successful.");
			
			return Response.ok(g.toJson(users)).build();		
			
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	}
	
}
