package gr.agroknow.metadata.identifier;

import java.util.Set;

public class IdentificationBean {

    private int docId;
    private Set<String> locations;
    private Set<String> identifiers;

   public int getdocId() {
	return docId;
	}

   public void setdocId(int ID) {
	this.docId=ID;
	}

   public Set<String> getLocations() {
		return locations;
	}

    public void setLocations(Set<String> locations) {
		this.locations = locations;
	}

    public Set<String> getIdentifiers() {
		return identifiers;
	}

    public void setIdentifiers(Set<String> identifiers) {
            this.identifiers = identifiers;
	}


}
