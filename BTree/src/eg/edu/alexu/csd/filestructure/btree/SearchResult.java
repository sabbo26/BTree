package eg.edu.alexu.csd.filestructure.btree;

public class SearchResult implements ISearchResult {

    private String id ;
    private int rank ;

    public SearchResult() {
    }

    public SearchResult(String id, int rank) {
        this.id = id;
        this.rank = rank;
    }

    @Override
    public String getId() {
        return this.id ;
    }

    @Override
    public void setId(String id) {
        this.id = id ;
    }

    @Override
    public int getRank() {
        return this.rank ;
    }

    @Override
    public void setRank(int rank) {
        this.rank = rank ;
    }
}
