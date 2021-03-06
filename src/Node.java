

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Node
{
	public static int nodeID;
	public static int outgoingNodeID;
	public static int duration;
	public static String message;
	//public static String lastRead;
	public static int lastCount;
	public static ArrayList<String> neighbours = new ArrayList<String>();
	public static String[] intree = new String[10];
	public static long[] lastHelloArray = new long[10];
	public static int lastDeletedNode = -1;
	public static boolean deleted = false;
	
	static List<Integer> usedNodes = new ArrayList<>();
	static List<Integer> nextCheck = new ArrayList<>();
	static List<Integer> tempCheck = new ArrayList<>();
	static String newIntree;

	public static void main(String args[])
	{
		// User will let the node know its nodeID
		// node 9 100 5 "this is a message" &
		// 0 100 3 "message from 0 to 3" &
		if (args.length > 0)
		{
			try
			{
				//System.out.println(Arrays.toString(args));
				nodeID = Integer.parseInt(args[0]);
				duration = 1000*Integer.parseInt(args[1]);
				outgoingNodeID = Integer.parseInt(args[2]);
				if(outgoingNodeID != -1)
				{
					message = args[3];
				}
				/*System.out.println("nodeID:"+nodeID);
				System.out.println("duration:"+duration);
				System.out.println("outgoingNodeID:"+outgoingNodeID);
				System.out.println("message:"+message);*/
		    }
			catch (Exception e)
			{
				System.out.println(e);
				System.err.println("Argument must be in proper format");
				//System.exit(1);
		    }
		}
		
		// Infinite loop
		long startTime = System.currentTimeMillis(); //fetch starting time
		long lastHello = startTime;
		long lastIntree = startTime;
		long lastData = startTime;
		while(System.currentTimeMillis()-startTime < duration)
		{
			// Hello message every 5 seconds
		    if(System.currentTimeMillis()-lastHello > 5000)
		    {
		    	//writeFile("hello "+nodeID+" "+System.currentTimeMillis());
		    	writeFile("hello "+nodeID);
		    	lastHello = System.currentTimeMillis();
		    }
		    if(System.currentTimeMillis()-lastIntree > 10000)
		    {
		    	writeFile(intree[nodeID]);
		    	lastIntree = System.currentTimeMillis();
		    }
		    if(outgoingNodeID != -1 && (System.currentTimeMillis()-lastData > 15000))
		    {
		    	sendData(nodeID, outgoingNodeID, message);
		    	lastData = System.currentTimeMillis();
		    }
		    // No Recent Hello Message Check
		    for(String neighbour : neighbours)
		    {
		    	if(System.currentTimeMillis() - lastHelloArray[Integer.valueOf(neighbour)] > 30000)
		    	{
		    		
		    		// System.out.println("NODE DEAD:"+neighbour);
		    		deleted = true;
		    		lastDeletedNode = Integer.valueOf(neighbour);

		    	}
		    }
		    if(deleted)
		    {
		    	neighbours.remove(String.valueOf(lastDeletedNode));
		    	deleted = false;
	    		createIntree();
	    		writeFile("delete "+lastDeletedNode);
		    }
		    // Reading input file for new messages
		    readFile();
		    //System.out.println("neighbours:"+neighbours);

		    try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("node "+nodeID+" stopped");
	}
	
	public static void sendData(int source, int destination, String message)
	{
		// 0 100 3 "message from 0 to 3" &
		int incomingNeighbour = -1;
		try
		{
			if (!neighbours.contains(String.valueOf(destination)))
			{
				//System.out.println("neighbors:"+neighbours+" destination"+destination);
				//System.out.println("destination not neighbour");
				incomingNeighbour = calculateIncomingNeighbour(destination);
			}
			else
			{
				//System.out.println("destination neighbour");
				incomingNeighbour = destination;
			}
			
			String path = pathToIncomingNeighbour(incomingNeighbour);
			if (path == null || incomingNeighbour == -1)
			{
				Exception myException = new Exception();
				throw myException;
			}
			//System.out.println("PATH:"+path);
			// data A E C B begin message
			String finalMessage = "data "+source+" "+destination+" "+path+"begin "+message;
			//System.out.println("finalmessage:"+finalMessage);
			writeFile(finalMessage);
		}
		catch(Exception e)
		{
			// Message cannt be sent. No Path.
			//System.out.println("NO PATH");
		}
	}
	
	public static String pathToIncomingNeighbour(int incomingNeighbour)
	{
		int[][] readIntreeMatrix = returnMatrix(intree[incomingNeighbour]);
		//System.out.println("Neighbor intree:"+intree[incomingNeighbour]);
		String path = "";
		List<Integer> nextHop = new ArrayList<>();
		nextHop.add(nodeID);
		Boolean pathFound=false;
		while(!nextHop.isEmpty())
		{
			for(int i=0;i<10;i++)
			{
				if(readIntreeMatrix[nextHop.get(0)][i] == 1)
				{
					if (i == incomingNeighbour)
					{
						path += i+" ";
						//System.out.println("edge:"+nextHop.get(0)+"-"+i);
						//System.out.println("PATH:"+ path);
						return path;
					}
					//System.out.println("edge:"+nextHop.get(0)+"-"+i);
					path += i+" ";
					nextHop.clear();
					nextHop.add(i);
					pathFound = true;
				}
			}
			if(pathFound == false)
			{
				return null;
			}
		}
		//System.out.println("PATH:"+ path);
		return null;
	}
	
	public static int calculateIncomingNeighbour(int destination)
	{
		int destNode = destination;
		int[][] myIntreeMatrix = returnMatrix(intree[nodeID]);
		List<Integer> nextHop = new ArrayList<>();
		nextHop.add(destNode);
		Boolean pathFound=false;
		while(!nextHop.isEmpty())
		{
			for(int i=0;i<10;i++)
			{
				if(myIntreeMatrix[nextHop.get(0)][i] == 1)
				{
					if (i == nodeID)
					{
						//System.out.println("DEST:"+nextHop.get(0));
						return nextHop.get(0);
					}
					nextHop.clear();
					nextHop.add(i);
					pathFound = true;
				}
			}
			if(pathFound == false)
			{
				return -1;
			}
		}

		return -1;
	}
	
	// Merging Intree
	public static void mergeIntree(String readIntree)
	{
		int[][] myIntreeMatrix = returnMatrix(intree[nodeID]);
		int[][] readIntreeMatrix = returnMatrix(readIntree);
		int[][] newMatrix = new int[10][10];
		
		//System.out.println("Old Intree:"+intree[nodeID]);
		//System.out.println("Received Intree:"+readIntree);
		
		// Merge Matrix
		for(int i=0;i<newMatrix.length;i++)
		{
			for(int j=0;j<newMatrix.length;j++)
			{
				// Boolean addition
				if(myIntreeMatrix[i][j] == 1 || readIntreeMatrix[i][j] == 1)
				{
					newMatrix[i][j] = 1;
				}
			}
		}
		//printMatrix(newMatrix);
		// Converting matrix to string
		usedNodes.clear();
		newIntree = "intree "+nodeID;
		nextCheck.add(nodeID);
		usedNodes.add(nodeID);
		while(!nextCheck.isEmpty())
		{
			generateIntree(newMatrix);
		}
		
		//System.out.println("INTREE:"+newIntree);
		intree[nodeID] = newIntree;
		//System.exit(0);
		
	}

	public static void generateIntree(int[][] newMatrix)
	{
		
		//printMatrix(newMatrix);
		tempCheck.clear();
		tempCheck.addAll(nextCheck);
		nextCheck.clear();
		//System.out.println("tempCheck:"+tempCheck);
		for(int nextInt: tempCheck)
		{
			//System.out.println("nextInt:"+nextInt);
			for(int i=0;i<newMatrix.length;i++)
			{
				//usedNodes.add(nextInt);
				//System.out.println("Added to used nodes:"+nextInt);
				//System.out.println("iXnextInt:"+newMatrix[i][nextInt]);
				if(newMatrix[i][nextInt] == 1 && !usedNodes.contains(i))
				{
					newIntree += " ( "+ i + " " + nextInt + " )";
					nextCheck.add(i);
					usedNodes.add(i);
					//System.out.println("Added to next nodes:"+i);
				}
			}
		}
	}
	
	public static int[][] returnMatrix(String tree)
	{
		int[][] matrix = new int[10][10];
		String[] tokens = tree.split(" ");
		for(int i=0;i<tokens.length;i++)
		{
			if(tokens[i].equals("("))
			{
				//System.out.println("Row:"+Integer.valueOf(tokens[i+1]));
				//System.out.println("Column:"+Integer.valueOf(tokens[i+2]));
				matrix[Integer.valueOf(tokens[i+1])][Integer.valueOf(tokens[i+2])] = 1;
			}
		}
		return matrix;
	}

	// Making default Intree from Neighbours List
	public static void createIntree()
	{
		intree[nodeID] = "intree "+nodeID;
		for(String neighbour : neighbours)
		{
			intree[nodeID] += " ( "+neighbour+" "+nodeID+" )";
		}
		//System.out.println("Hello Intree at "+nodeID+" :"+intree[nodeID]);
	}
	
    // Reading File
    public static void readFile()
    {
    	try
    	{
    		Boolean readAllow = false;
    		String str = "input_"+nodeID+".txt";
    		BufferedReader ReadFile = new BufferedReader(new FileReader(str));
    		int temp = 0;
    		while((str = ReadFile.readLine()) != null)
    		{
    			String[] tokens = str.split(" ");
    			// First message
    			
    			++temp;
    			if(temp > lastCount)
    			{
    				// Reading Hello Protocol messages
    				// hello 2 1399591802089
    				if(tokens[0].equals("hello"))
    				{
    					// Putting Hello TS to array
    					lastHelloArray[Integer.valueOf(tokens[1])] = System.currentTimeMillis();
    					
    					// Find Neighbours
    					if(!neighbours.contains(tokens[1]))
    					{
    						// New neighbours discovered
    						neighbours.add(tokens[1]);
    						createIntree();
    					}
    				}
    				
    				// Reading intree protocol messages
    				// intree 2 ( 0 2 ) ( 1 2 )
    				if(tokens[0].equals("intree") && !str.contains(String.valueOf(lastDeletedNode)))
    				{
    					// storing intree for source routing
    					intree[Integer.valueOf(tokens[1])] = str;
    					mergeIntree(str);
    				}
    				// data A E C B begin message
    				// 0    1 2 3 4 5
    				// data 0 3 3 begin message from 0 to 3
    				// data 2 4 1 begin message from 2 to 4
    				// data 2 4 2 3 0 begin message from 2 to 4 FWD
    				// data 2 4 0 begin message from 2 to 4 new route
    				// data 2 4 4 3 begin message from 2 to 4
    				if(tokens[0].equals("data"))
    				{
    					// storing intree for source routing
    					// Destination check
    					if(Integer.valueOf(tokens[2]) == nodeID && Integer.valueOf(tokens[3]) == nodeID && tokens[4].equals("begin"))
    					{
    						String message="";
							for(int i=5;i<tokens.length;i++)
							{
								// message will have extra space at end
								// TODO: Fix
								message += tokens[i]+" ";
							}
    						//System.out.println("MESSAGE RECEIVED:"+message);
    						writeMessage("message from "+tokens[1]+":"+message);
    					}
    					// Intermediate check
    					else if(Integer.valueOf(tokens[3]) == nodeID)
    					{
    						if(tokens[4].equals("begin"))
    						{
    							//New Source Routing
    							String message="";
    							for(int i=5;i<tokens.length;i++)
    							{
    								// message will have extra space at end
    								// TODO: Fix
    								message += tokens[i]+" ";
    							}
    							//System.out.println("New Souce Routing message:"+message);
    							//StringUtils.stripEnd(message," ");
    							int source = Integer.valueOf(tokens[1]);
    							int destination = Integer.valueOf(tokens[2]);
    							sendData(source, destination, message);
    							
    						}
    						else
    						{
    							// Forward data to tokens[4]
    							String newMessage="data "+tokens[1]+" "+tokens[2];
    							for(int i=4;i<tokens.length;i++)
    							{
    								newMessage += " "+tokens[i];
    							}
    							writeFile(newMessage);
    						}
    					}
    					else
    					{
    						// IGNORE MESSAGE
    					}
    				}
    				if(tokens[0].equals("delete") && lastDeletedNode != Integer.valueOf(tokens[1]))
    				{
    		    		// System.out.println("NODE DEAD:"+neighbour);
    		    		lastDeletedNode = Integer.valueOf(tokens[1]);
    		    		createIntree();
    		    		writeFile(str);
    				}
    			}
    			
    		}
    		lastCount = temp;
        }
        catch(Exception e)
        {
            // System.out.println(e + " in readFile()");
        }
    }
    
    // Writing File
    public static void writeFile(String message)
    {
    	try
    	{                              
    		String str = message;
    		String filePath = "output_"+nodeID+".txt";
    		// Append mode
    		BufferedWriter WriteFile = new BufferedWriter(new FileWriter(filePath,true));
    		WriteFile.write(str);
    		WriteFile.write("\n");
    		WriteFile.close();
        }
        catch(Exception e)
        {
            //System.out.println(e + " in writeFile()");
        }
    }
    
 // Writing Message
    public static void writeMessage(String message)
    {
    	try
    	{                              
    		String str = message;
    		String filePath = nodeID+"_received"+".txt";
    		// Append mode
    		BufferedWriter WriteFile = new BufferedWriter(new FileWriter(filePath,true));
    		WriteFile.write(str);
    		WriteFile.write("\n");
    		WriteFile.close();
        }
        catch(Exception e)
        {
            //System.out.println(e + " in writeFile()");
        }
    }
    
	public static void printMatrix(int[][] matrix)
	{
		System.out.println("Length:"+matrix.length);
		for (int i = 0; i < 10; i++) {
		    for (int j = 0; j < 10; j++) {
		        System.out.print(matrix[i][j] + " ");
		    }
		    System.out.print("\n");
		}
	}
    
}
