package de.hdm.drools;

import java.net.URI;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpStatus;

import de.hdm.drools.anmeldung.BahnhofAnmeldung;
import de.hdm.drools.anmeldung.KnotenpunktAnmeldung;
import de.hdm.drools.anmeldung.ZugAnmeldung;
import de.hdm.drools.nachricht.FahrtAbschluss;
import de.hdm.drools.nachricht.FahrtAnfrage;
import de.hdm.drools.nachricht.FahrtAnfrageMitAsyncResponse;
import de.hdm.drools.nachricht.FahrtBeginn;
import de.hdm.drools.nachricht.FahrtBeginnMitAsyncResponse;
import de.hdm.drools.nachricht.Lebenszeichen;
import de.hdm.drools.resource.Bahnhof;
import de.hdm.drools.resource.Knotenpunkt;
import de.hdm.drools.resource.Strecke;
import de.hdm.drools.resource.Zug;

/**
 * REST-Schnittstelle der Netzverwaltung, geschrieben in Jersey.
 * @author Andreas Herrmann
 * @param knotenpunkte Vector, der alle bekannten Knotenpunkte enthält
 * @param bahnhoefe Vector, der alle bekannten Bahnhöfe enthält
 * @param zuege Vector, der alle bekannten Züge enthält
 * @param strecken Vector, der alle bekannten Strecken enthält
 */
@Path("/")
public class Netzverwaltung {
	private static Vector<Knotenpunkt> knotenpunkte = new Vector<Knotenpunkt>();
	private static Vector<Bahnhof> bahnhoefe = new Vector<Bahnhof>();
	private static Vector<Zug> zuege = new Vector<Zug>();
	private static Vector<Strecke> strecken = new Vector<Strecke>();
	
	/**
	 * Fügt dem Vector knotenpunkte einen neuen Knotenpunkt hinzu.
	 * @param knotenpunkt Der Knotenpunkt, der hinzugefügt werden soll
	 */
	public static void addKnotenpunkt(Knotenpunkt knotenpunkt){
		knotenpunkte.add(knotenpunkt);
		KnowledgeBaseThread.addKnotenpunkt(knotenpunkt);
	}
	/**
	 * Fügt dem Vector bahnhoefe einen neuen Bahnhof hinzu.
	 * @param bahnhof Der Bahnhof, der hinzugefügt werden soll
	 */
	public static void addBahnhof(Bahnhof bahnhof){
		bahnhoefe.add(bahnhof);
		KnowledgeBaseThread.addBahnhof(bahnhof);
	}
	/**
	 * Fügt dem Vector zuege einen neuen Zug hinzu.
	 * @param zug Der Zug, der hinzugefügt werden soll
	 */
	public static void addZug(Zug zug){
		zuege.add(zug);
		KnowledgeBaseThread.addZug(zug);
	}
	/**
	 * Fügt dem Vector strecken eine neue Strecke hinzu.
	 * @param strecke Die Strecke, die hinzugefügt werden soll
	 */
	public static void addStrecke(Strecke strecke){
		strecken.add(strecke);
		KnowledgeBaseThread.addStrecke(strecke);
	}
	
	@Path("/knotenpunkt/anmelden")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void knotenpunktAnmelden(@Context HttpServletRequest req,
			@Suspended final AsyncResponse asyncResponse){
		URI adresse = URI.create(req.getRemoteAddr());
		for(int j=0;j<bahnhoefe.size();j++){
			if(adresse.equals(bahnhoefe.get(j))){
				asyncResponse.resume(Response.status(HttpStatus.BAD_REQUEST_400).build());
			}
		}
		KnowledgeBaseThread.knotenpunktAnmelden(new KnotenpunktAnmeldung(asyncResponse, adresse));		
	}
	
	@Path("/knotenpunkt/abmelden")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response knotenpunktAbmelden(@Context HttpServletRequest req,
			@NotNull Knotenpunkt knotenpunkt){
		URI adresse=URI.create(req.getRemoteAddr());
		if(!adresse.equals(knotenpunkt.getAdresse())){
			return Response.status(HttpStatus.FORBIDDEN_403).build();
		}
		else{
			for(int i=0;i<knotenpunkte.size();i++){
				if(knotenpunkt.equals(knotenpunkte.get(i))){
					knotenpunkte.get(i).setAdresse(null);
					KnowledgeBaseThread.knotenpunktAbmelden(knotenpunkt);
					return Response.ok().build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/bahnhof/anmelden")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void bahnhofAnmelden(@Context HttpServletRequest req,
			@Suspended final AsyncResponse asyncResponse){
		URI adresse = URI.create(req.getRemoteAddr());
		for(int j=0;j<bahnhoefe.size();j++){
			if(adresse.equals(bahnhoefe.get(j))){
				asyncResponse.resume(Response.status(HttpStatus.BAD_REQUEST_400).build());
			}
		}
		KnowledgeBaseThread.bahnhofAnmelden(new BahnhofAnmeldung(asyncResponse, adresse));
	}
	
	@Path("/bahnhof/abmelden")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response bahnhofAbmelden(@Context HttpServletRequest req,
			@NotNull Bahnhof bahnhof){
		URI adresse=URI.create(req.getRemoteAddr());
		if(!adresse.equals(bahnhof.getAdresse())){
			return Response.status(HttpStatus.FORBIDDEN_403).build();
		}
		else{
			for(int i=0;i<bahnhoefe.size();i++){
				if(bahnhof.equals(bahnhoefe.get(i))){
					bahnhoefe.get(i).setAdresse(null);
					KnowledgeBaseThread.bahnhofAbmelden(bahnhof);
					return Response.ok().build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/zug/anmelden")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void zugAnmelden(@Context HttpServletRequest req,
			@Suspended final AsyncResponse asyncResponse){
		URI adresse = URI.create(req.getRemoteAddr());
		for(int j=0;j<bahnhoefe.size();j++){
			if(adresse.equals(bahnhoefe.get(j))){
				asyncResponse.resume(Response.status(HttpStatus.BAD_REQUEST_400).build());
			}
		}
		KnowledgeBaseThread.zugAnmelden(new ZugAnmeldung(asyncResponse, adresse));
	}
	
	@Path("/zug/abmelden")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response zugAbmelden(@Context HttpServletRequest req,
			@NotNull Zug zug){
		URI adresse=URI.create(req.getRemoteAddr());
		if(!adresse.equals(zug.getAdresse())){
			return Response.status(HttpStatus.FORBIDDEN_403).build();
		}
		else{
			for(int i=0;i<zuege.size();i++){
				if(zug.equals(zuege.get(i))){
					zuege.get(i).setAdresse(null);
					KnowledgeBaseThread.zugAbmelden(zug);
					return Response.ok().build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/fahrt/anfrage")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void 	fahrtAnfrageStellen(@Context HttpServletRequest req,
			@Suspended final AsyncResponse asyncResponse,
			@NotNull FahrtAnfrage fahrtanfrage){
		URI adresse=URI.create(req.getRemoteAddr());
		if(!adresse.equals(fahrtanfrage.getZug().getAdresse())){
			asyncResponse.resume(Response.status(HttpStatus.FORBIDDEN_403).build());
		}
		else{
			for(int i=0;i<strecken.size();i++){
				if(fahrtanfrage.getStrecke().equals(strecken.get(i))){
					KnowledgeBaseThread
					.fahrtAnfragen(new FahrtAnfrageMitAsyncResponse(fahrtanfrage
							,asyncResponse));
				}
			}
			asyncResponse.resume(Response.status(HttpStatus.NOT_FOUND_404).build());
		}
	}
	
	@Path("/fahrt/beginn")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void 	fahrtBeginnMelden(@Context HttpServletRequest req,
			@Suspended final AsyncResponse asyncResponse,
			@NotNull FahrtBeginn fahrtbeginn){
		URI adresse=URI.create(req.getRemoteAddr());
		if(!adresse.equals(fahrtbeginn.getZug().getAdresse())){
			asyncResponse.resume(Response.status(HttpStatus.FORBIDDEN_403).build());
		}
		else{
			for(int i=0;i<strecken.size();i++){
				if(fahrtbeginn.getStrecke().equals(strecken.get(i))){
					KnowledgeBaseThread
					.fahrtBeginn(new FahrtBeginnMitAsyncResponse(fahrtbeginn,
							asyncResponse));
				}
			}
			asyncResponse.resume(Response.status(HttpStatus.NOT_FOUND_404).build());
		}
	}
	
	@Path("/fahrt/abschluss")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response fahrtAbschlussMelden(@Context HttpServletRequest req,
			@NotNull FahrtAbschluss fahrtabschluss){
		URI adresse=URI.create(req.getRemoteAddr());
		if(!adresse.equals(fahrtabschluss.getZug().getAdresse())){
			return Response.status(HttpStatus.FORBIDDEN_403).build();
		}
		else{
			for(int i=0;i<strecken.size();i++){
				if(fahrtabschluss.getStrecke().equals(strecken.get(i))){
					KnowledgeBaseThread
					.fahrtAbschluss(fahrtabschluss);
					return Response.ok().build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/fahrt/lebenszeichen")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response lebenszeichenAbsetzen(@Context HttpServletRequest req,
			@NotNull Lebenszeichen lebenszeichen){
		URI adresse=URI.create(req.getRemoteAddr());
		if(!adresse.equals(lebenszeichen.getZug().getAdresse())){
			return Response.status(HttpStatus.FORBIDDEN_403).build();
		}
		else{
			for(int i=0;i<strecken.size();i++){
				if(lebenszeichen.getStrecke().equals(strecken.get(i))){
					KnowledgeBaseThread.lebenszeichen(lebenszeichen);
					return Response.ok().build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/knotenpunkt/{ID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response findKnotenpunktByID(@PathParam("ID") long id){
		if(id<=0){
			return Response.status(HttpStatus.BAD_REQUEST_400).build();
		}
		else{
			for(int i=0; i<knotenpunkte.size();i++){
				if(knotenpunkte.get(i).getID()==id){
					return Response.ok(knotenpunkte.get(i)).build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/bahnhof/{ID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response findBahnhofByID(@PathParam("ID") long id){
		if(id<=0){
			return Response.status(HttpStatus.BAD_REQUEST_400).build();
		}
		else{
			for(int i=0; i<bahnhoefe.size();i++){
				if(bahnhoefe.get(i).getID()==id){
					return Response.ok(bahnhoefe.get(i)).build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/zug/{ID}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response findZugByID(@PathParam("ID") long id){
		if(id<=0){
			return Response.status(HttpStatus.BAD_REQUEST_400).build();
		}
		else{
			for(int i=0; i<zuege.size();i++){
				if(zuege.get(i).getID()==id){
					return Response.ok(zuege.get(i)).build();
				}
			}
			return Response.status(HttpStatus.NOT_FOUND_404).build();
		}
	}
	
	@Path("/strecke/zu/knotenpunkt")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findStreckeZuKnotenpunktHin(@NotNull Knotenpunkt knotenpunkt){
		Vector<Strecke> gefundeneStrecken = new Vector<Strecke>();
		for(int i=0;i<strecken.size();i++){
			if(strecken.get(i).getZiel().equals(knotenpunkt)){
				gefundeneStrecken.add(strecken.get(i));
			}
		}
		if(gefundeneStrecken.size()<1){
			return Response.noContent().build();
		}
		else{
			return Response.ok(gefundeneStrecken.toArray()).build();
		}
	}
	
	@Path("/strecke/von/knotenpunkt")
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response findStreckeVonKnotenpunktWeg(@NotNull Knotenpunkt knotenpunkt){
		Vector<Strecke> gefundeneStrecken = new Vector<Strecke>();
		for(int i=0;i<strecken.size();i++){
			if(strecken.get(i).getStart().equals(knotenpunkt)){
				gefundeneStrecken.add(strecken.get(i));
			}
		}
		if(gefundeneStrecken.size()<1){
			return Response.noContent().build();
		}
		else{
			return Response.ok(gefundeneStrecken.toArray()).build();
		}
	}
}