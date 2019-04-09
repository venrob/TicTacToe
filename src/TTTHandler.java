
import java.util.LinkedList;

public class TTTHandler extends Thread
{
    TTTGui client;
    TTTBoard board;
    int ID;
    private volatile LinkedList<String> instructions = new LinkedList<>();
    public TTTHandler(TTTGui client){
        this.client = client;
    }
    public void run()
    {
        try
        {
            while(!this.isInterrupted())
            {
                try
                {
                    while(board == null) takeClientTurn(false);
                    if(this.isInterrupted()) return;
                    for(int curID = 1; board.checkWinner()==0; curID = (curID % 2) + 1) //Alternate 1 and 2
                    {
                        if(this.ID == curID)
                            takeClientTurn(true);
                        else ;//Take AI turn
                        if(board.checkWinner() != 0)break;
                        if(this.ID != curID)
                            takeClientTurn(true);
                        else ;//Take AI turn
                    }
                    client.endGame(board.checkWinner());
                }
                catch(TTTException e)
                {
                    if(!e.getMessage().equals("Game exiting to main menu!")) throw e;
                }
                board = null; ID = 0; instructions.clear();
            }
        }
        catch(Exception e)
        {
            if(e.getMessage() != null) System.err.println(e.getMessage());
            else e.printStackTrace();
            System.err.println("Exiting thread...");
        }
    }
    
    private void takeClientTurn(boolean isGameTurn) throws TTTException
    {
        instructions.clear();
        if(isGameTurn) client.turnStart(true);
        while(!this.isInterrupted())
        {
            while(instructions.isEmpty()) //Wait for instructions from either side.
                try {
                    sleep((int)(1000*0.2));
                } catch (InterruptedException e) {return;}
            while(!instructions.isEmpty())
            {
                String instruction = instructions.pollFirst().toLowerCase();
                String params[] = instruction.split("\\s",2);
                instruction = params[0];
                params = params[1].split("\\s");
                //`instruction` is the head command
                //`params` are the sub-arguments
                switch(instruction)
                {
                    case "place":
                        if(board==null)break; //No placing when no board exists
                        int x = Integer.parseInt(params[0]);
                        int y = Integer.parseInt(params[1]);
                        if(board.getSpace(x, y)==0)
                        {
                            board.setSpace(x,y,ID);
                            client.updateButtons(board);
                            if(isGameTurn) client.turnStart(false);
                            return;
                        }
                        break;
                    case "start":
                        ID = Integer.parseInt(params[0]);
                        board = new TTTBoard();
                        //Add AI client, with opposing ID
                        //opposingID = 3 - ID;
                        if(isGameTurn) client.turnStart(false);
                        return;
                    case "menu":
                        throw new TTTException("Game exiting to main menu!");
                }
            }
        }
    }
    
    public void addInstruction(String instruction)
    {
        instructions.add(instruction);
    }
}