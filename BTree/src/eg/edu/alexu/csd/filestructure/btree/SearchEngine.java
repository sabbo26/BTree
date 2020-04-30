package eg.edu.alexu.csd.filestructure.btree;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SearchEngine implements ISearchEngine {

    private List<String> IDs ;
    private List<IBTree<String,Integer>> words ;
    private int minDegree ;

    public SearchEngine(int minimumDegree) {
        this.minDegree = minimumDegree ;
        words = new LinkedList<>();
        IDs = new LinkedList<>();
    }

    @Override
    public void indexWebPage(String filePath) {
        if(filePath == null || isCorrupt(filePath) ) throw new RuntimeErrorException(new Error("invalid path"));
        File toIndex = new File(filePath);
        if(!toIndex.exists()) throw new RuntimeErrorException(new Error("invalid path"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder parser = factory.newDocumentBuilder();
            try {
                Document temp =  parser.parse(toIndex);
                NodeList samples = temp.getElementsByTagName("doc");
                for (int i = 0 ; i < samples.getLength() ; i++ ){
                    Node x = samples.item(i);
                    if( x.getNodeType() == Node.ELEMENT_NODE){
                        Element finall = (Element) x ;
                        IDs.add(finall.getAttribute("id"));
                        NodeList text = finall.getChildNodes();
                        for( int j = 0 ; j < text.getLength() ; j++){
                            Node t = text.item(j);
                            String h = t.getTextContent();
                            words.add(indexDocument(h));
                        }
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }catch (ParserConfigurationException ex ){
            ex.printStackTrace();
        }
    }

    @Override
    public void indexDirectory(String directoryPath) {
        if(directoryPath == null || isCorrupt(directoryPath) ) throw new RuntimeErrorException(new Error("invalid path"));
        File toIndex = new File(directoryPath);
        if(!toIndex.exists()) throw new RuntimeErrorException(new Error("invalid path"));
        File[] files = toIndex.listFiles();
        if (files != null ){
            for(int i = 0 ; i < files.length ; i++){
                indexDirectory(files[i].getPath());
            }
        }
        else
            indexWebPage(directoryPath);

    }

    @Override
    public void deleteWebPage(String filePath) {
        if(filePath == null || isCorrupt(filePath) ) throw new RuntimeErrorException(new Error("invalid path"));
        File toDelete = new File(filePath);
        if(!toDelete.exists()) throw new RuntimeErrorException(new Error("invalid path"));
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder parser = factory.newDocumentBuilder();
            try {
                Document temp =  parser.parse(toDelete);
                NodeList samples = temp.getElementsByTagName("doc");
                Node x = samples.item(0);
                if( x.getNodeType() == Node.ELEMENT_NODE){
                    Element w = (Element) x ;
                    String h = w.getAttribute("id");
                    int t = this.IDs.indexOf(h);
                    if(t == -1 )
                        return ;
                    else{
                        for (int i = 0 ; i < samples.getLength() ; i++){
                            this.IDs.remove(t);
                            this.words.remove(t);
                        }
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }catch (ParserConfigurationException ex ){
            ex.printStackTrace();
        }
    }

    @Override
    public List<ISearchResult> searchByWordWithRanking(String word) {
        if( word == null)
            throw new RuntimeErrorException(new Error("null word "));
        List<ISearchResult> results = new LinkedList<>();
        if (isCorrupt(word))
            return results ;
        word = word.toLowerCase() ;
        for (int i = 0 ; i < words.size() ; i++ ){                  //check for the word in each article
            Integer h = words.get(i).search(word);
            if (h != null){
                results.add(new SearchResult(this.IDs.get(i) , h ));
            }
        }
        return results ;
    }

    @Override
    public List<ISearchResult> searchByMultipleWordWithRanking(String sentence) {
        if( sentence == null)
            throw new RuntimeErrorException(new Error("null word "));
        List<ISearchResult> results = new LinkedList<>();
        if (isCorrupt(sentence))
            return results ;
        sentence = sentence.toLowerCase();
        removeSpaces(sentence);
        String[] x = sentence.split(" ");
        for( int i = 0 ; i < words.size() ; i++ )            //check in every article for the three words
        {
            boolean isFound = true ;
            Integer[] h = new Integer[x.length];
            for (int j = 0 ; j < x.length ; j++){
                h[j] = words.get(i).search(x[j]) ;
                if(h[j] ==  null)
                    isFound = false ;
            }
            if (isFound)
                results.add(new SearchResult(IDs.get(i) , min(h) ) ) ;
        }
        return results ;
    }

    private void removeSpaces (String x){
        for (int i = 0 ; i < x.length() ; i++ ){
            if(x.charAt(i) == ' '){
                if(i == x.length()-1)
                    x = x.substring(0,x.length()-1);
                else if ( i == 0 ){
                    x = x.substring(1);
                    i-- ;
                }
                else if ( x.charAt(i+1) == ' '){
                    x = x.substring(0,i)+x.substring(i+1);
                    i-- ;
                }
            }
        }
    }    //remove extra spaces from a sentence

    private boolean isCorrupt (String filePath){
        for (int i = 0 ; i < filePath.length() ; i++){
            if(filePath.charAt(i) != ' ')
                return false ;
        }
        return true ;
    }    //check if the word is just spaces

    private IBTree<String,Integer> indexDocument (String text){
        text = text.toLowerCase();
        for (int i= 0;i< text.length() ; i++){
            if(text.charAt(i)=='\n'){
                if (i == 0){
                    text = text.substring(1);
                    i-- ;
                }
                else if ( i == text.length()-1){
                    text = text.substring(0,text.length()-1);
                }
                else{
                    text = text.substring(0,i) + text.substring(i+1);
                    if(text.charAt(i) == '\n')
                        i-- ;
                    else{
                        text = text.substring(0 , i) + " " + text.substring(i);
                    }
                }
            }
        }
        IBTree<String,Integer> temp = new BTree<>(this.minDegree);
        String[] x = text.split(" ");
        for (int i = 0 ; i < x.length ; i++ ){
            Integer h = temp.search(x[i]) ;
            if(h != null ){
                temp.delete(x[i]);
                temp.insert(x[i],h+1);
            }
            else{
                temp.insert(x[i],1);
            }
        }
        return temp ;
    }    // parse text to words and count their frequencies and add them to B tree

    private int min (Integer[] arr){
        int min = arr[0];
        for(int i = 1 ; i < arr.length ; i++){
            if(arr[i] < min)
                min = arr[i];
        }
        return min ;
    }

    public static void main(String[] args) {
        SearchEngine sayed = new SearchEngine(3);
        sayed.indexWebPage("res//wiki_00");
    }
}
