package pt.unl.fct.di.apdc.firstwebapp.resources;

import java.util.ArrayList;
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
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.firstwebapp.util.ListData;


@Path("/list")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListResource {
	
	/**
	 * Logger object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Gson g = new Gson();

	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public ListResource() {}
	
	@POST
	@Path("/v1")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listUsers(ListData data) {
		LOG.fine("Attempt to listUsers by " + data.username);
		
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Key userTokenKey = datastore.newKeyFactory().setKind("Token").newKey(data.username);
		
		Transaction txn = datastore.newTransaction();
		
		try {
			Entity user = txn.get(userKey);
			
			if(user == null) {
				txn.rollback();
				LOG.warning("ListUsers operation canceled, " + data.username + " does not exist.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " does not exist.").build();
			}
			
			Entity userToken = txn.get(userTokenKey);
			
			if(userToken == null) {
				txn.rollback();
				LOG.warning("ListUsers operation canceled, " + data.username + " is not logged in.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " is not logged in.").build();
			}
			
			if(!isTokenValid(userToken)) {
				LogoutResource invalidTokenAux = new LogoutResource();
				invalidTokenAux.logout(data.username);

				txn.rollback();
				LOG.warning("ListUsers operation canceled, " + data.username + " session has expired.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " session has expired.").build();
			}
			
			if (user.getString("state").equals("INATIVO")) {
				txn.rollback();
				LOG.warning("ListUsers operation canceled, " + data.username + " is not active.");

				return Response.status(Status.BAD_REQUEST).entity(data.username + " is not active.").build();
			}
			
			List<String> users = new ArrayList<String>();
			String userRole = user.getString("role");
			switch(userRole) {
				case "USER":
					Query<Entity> queryUSER = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(
							   PropertyFilter.eq("role", "USER"),
							   PropertyFilter.eq("profile", "Publico"), 
							   PropertyFilter.eq("state", "ATIVO")))
							   .build();
					
					QueryResults<Entity> queryUserList = datastore.run(queryUSER);

					users.add("---------------------------------------------------,");
					queryUserList.forEachRemaining(usersList -> {
						users.add("Username:" + usersList.getString("username") + 
									"; email:" + usersList.getString("email")
									+ "; name:" + usersList.getString("name"));
						users.add(",---------------------------------------------------" + ",,");
					});
					
					break;
				case "GBO":
					Query<Entity> queryGBO = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(
							   PropertyFilter.eq("role", "USER")))
							   .build();

					QueryResults<Entity> queryGBOList = datastore.run(queryGBO);
					
					users.add("---------------------------------------------------,");
					queryGBOList.forEachRemaining(usersList -> {
						users.add("Username: " + usersList.getString("username") 
									+ "; email: " + usersList.getString("email")
									+ "; name: " + usersList.getString("name"));
						users.add("Profile: " + usersList.getString("profile") 
									+ "; telephone: " + usersList.getString("telephone")
									+ "; mobile: " + usersList.getString("mobilePhone")
									+ ", occupation: " + usersList.getString("profession") 
									+ "; work place: " + usersList.getString("workPlace"));
						users.add("Address: " + usersList.getString("address"));
						users.add("Additional address: " + usersList.getString("additionalAddress"));
						users.add("City: " + usersList.getString("city") 
									+ "; zip: " + usersList.getString("zip")
									+ "; nif: " + usersList.getString("nif")
									+ "; role: " + usersList.getString("role")
									+ "; state: " + usersList.getString("state"));
						users.add(",---------------------------------------------------" + ",,");
					});
					break;
				case "GS":
					Query<Entity> queryGS = Query.newEntityQueryBuilder().setKind("User")
					.setFilter(CompositeFilter.and(PropertyFilter.eq("role", "USER")))
							   .build();

					QueryResults<Entity> queryGSList = datastore.run(queryGS);

					users.add("---------------------------------------------------,");
					queryGSList.forEachRemaining(usersList -> {
						users.add("Username: " + usersList.getString("username") 
									+ "; email: " + usersList.getString("email")
									+ "; name: " + usersList.getString("name"));
						users.add("Profile: " + usersList.getString("profile") 
									+ "; telephone: " + usersList.getString("telephone")
									+ "; mobile: " + usersList.getString("mobilePhone")
									+ ", occupation: " + usersList.getString("profession") 
									+ "; work place: " + usersList.getString("workPlace"));
						users.add("Address: " + usersList.getString("address"));
						users.add("Additional address: " + usersList.getString("additionalAddress"));
						users.add("City: " + usersList.getString("city") 
									+ "; zip: " + usersList.getString("zip")
									+ "; nif: " + usersList.getString("nif")
									+ "; role: " + usersList.getString("role")
									+ "; state: " + usersList.getString("state"));
						users.add(",---------------------------------------------------" + ",,");
					});
					
					Query<Entity> queryGS2 = Query.newEntityQueryBuilder().setKind("User")
							.setFilter(CompositeFilter.and(PropertyFilter.eq("role", "GBO")))
									   .build();

							QueryResults<Entity> queryGSList2 = datastore.run(queryGS2);

							users.add("---------------------------------------------------,");
							queryGSList2.forEachRemaining(usersList -> {
								users.add("Username: " + usersList.getString("username") 
											+ "; email: " + usersList.getString("email")
											+ "; name: " + usersList.getString("name"));
								users.add("Profile: " + usersList.getString("profile") 
											+ "; telephone: " + usersList.getString("telephone")
											+ "; mobile: " + usersList.getString("mobilePhone")
											+ ", occupation: " + usersList.getString("profession") 
											+ "; work place: " + usersList.getString("workPlace"));
								users.add("Address: " + usersList.getString("address"));
								users.add("Additional address: " + usersList.getString("additionalAddress"));
								users.add("City: " + usersList.getString("city") 
											+ "; zip: " + usersList.getString("zip")
											+ "; nif: " + usersList.getString("nif")
											+ "; role: " + usersList.getString("role")
											+"; state: " + usersList.getString("state"));
								users.add(",---------------------------------------------------" + ",,");
							});
					break;
				case "SU":
					Query<Entity> querySU = Query.newEntityQueryBuilder().setKind("User")
							   .setOrderBy(OrderBy.desc("userCreationTime"))
							   .build();

					QueryResults<Entity> querySUList = datastore.run(querySU);

					users.add("---------------------------------------------------,");
					querySUList.forEachRemaining(usersList -> {
						users.add("Username: " + usersList.getString("username") 
									+ "; email: " + usersList.getString("email")
									+ "; name: " + usersList.getString("name"));
						users.add("Profile: " + usersList.getString("profile") 
									+ "; telephone: " + usersList.getString("telephone")
									+ "; mobile: " + usersList.getString("mobilePhone")
									+ ", occupation: " + usersList.getString("profession") 
									+ "; work place: " + usersList.getString("workPlace"));
						users.add("Address: " + usersList.getString("address"));
						users.add("Additional address: " + usersList.getString("additionalAddress"));
						users.add("City: " + usersList.getString("city") 
							+ "; zip: " + usersList.getString("zip")
							+ "; nif: " + usersList.getString("nif")
							+ "; role: " + usersList.getString("role")
							+"; state: " + usersList.getString("state"));
						users.add(",---------------------------------------------------" + ",,");
					});
					break;
				default: 
					break;
			}
			
			LOG.info("Listing users by " + data.username + " was successful.");
			
			return Response.ok(g.toJson(users)).build();
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
