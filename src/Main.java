
import FlappyBird.*;
import Network.*;
import OrganicNN.*;
import TrainSet.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        GamePanel game = new GamePanel();
        game.start();

        // wait until training data is captured
        System.out.println("starting data capture...");
        System.out.println("press ENTER to begin training");
        try { System.in.read(); }
        catch(Exception e) { e.printStackTrace(); }

        // get captured game data and store in a TrainSet
        System.out.println("finished data capture...");
        ArrayList<double[][]> gameData = game.getDataSet(100);
        TrainSet trainingSet = new TrainSet(gameData.get(0)[0].length, gameData.get(0)[1].length);
        for (double[][] data : gameData) trainingSet.addData(data[0], data[1]);

        // create an ONN and train it using the collected data
        OrganicNN net = new OrganicNN("net.structure"); // 5 4 1
        System.out.println("training network... please wait");
        long time = System.nanoTime();
        net.train(trainingSet, 100000, 50, 0.3);
        time = System.nanoTime() - time;
        System.out.println("finished network training...");
        System.out.println("traing time (ms): " + time/1000000);
        game.loadONN(net);
    }
}
