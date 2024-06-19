package stamps;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StampTool implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean active;
    
    private List<int[][]> stampPatterns;
    
    public int currentStampIndex = 0;

    public StampTool() {
    	
        this.active = false;
        
        this.stampPatterns = new ArrayList<>();
    }

    public void addStampPattern(int[][] stamp) {
    	
        int[][] newStamp = new int[stamp.length][stamp[0].length];
        
        for (int y = 0; y < stamp.length; y++) {
        	
            System.arraycopy(stamp[y], 0, newStamp[y], 0, stamp[y].length);
        }
        
        stampPatterns.add(newStamp);
    }

    public int[][] getStampPattern(int index) {
    	
        if (index >= 0 && index < stampPatterns.size()) {
        	
            return stampPatterns.get(index);
        } 
        else {
        	
            return new int[0][0];
        }
    }

    public List<int[][]> getAllStampPatterns() {
    	
        return new ArrayList<>(stampPatterns);
    }

    public boolean isActive() {
    	
        return active;
    }

    public void toggle() {
    	
        active = !active;
    }

    public void clear() {
    	
        stampPatterns.clear();
        
        currentStampIndex = 0;
    }

    public void setActive(boolean active) {
    	
        this.active = active;
    }

    public int getCurrentStampIndex() {
    	
        return currentStampIndex;
    }

    public void setCurrentStampIndex(int index) {
    	
        if (index >= 0 && index < stampPatterns.size()) {
        	
            currentStampIndex = index;
        } 
        else {
        	
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
    }

    public void updateStampPattern(int index, int[][] stamp) {
    	
        if (index >= 0 && index < stampPatterns.size()) {
        	
            int[][] updatedStamp = new int[stamp.length][stamp[0].length];
            
            for (int y = 0; y < stamp.length; y++) {
            	
                System.arraycopy(stamp[y], 0, updatedStamp[y], 0, stamp[y].length);
            }
            
            stampPatterns.set(index, updatedStamp);
        } 
        else {
        	
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
    }

    public void saveToFile(String filename) throws IOException {
    	
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
        	
        	int[][] currentStamp = getStampPattern(currentStampIndex);
        	
            oos.writeObject(currentStamp);
        }
    }

    public int[][] loadFromFile(String filename) throws IOException, ClassNotFoundException {
    	
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
        	
            int[][] loadedPattern = (int[][]) ois.readObject();
            
            return loadedPattern;
        }
    }

}
