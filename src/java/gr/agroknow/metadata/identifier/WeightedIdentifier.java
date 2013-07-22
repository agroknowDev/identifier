package gr.agroknow.metadata.identifier;

public class WeightedIdentifier {

    private double weight ;
    private int docId;
    /*In case the file is of GREY AND FOUND*/

    public double getWeight() {
	return weight;
	}

   public void setWeight(double weight){
     this.weight= weight;
   }

    public int getDocId() {
	return docId;
	}

    public void setDocId(int docId){
      this.docId=docId;
   }


}
