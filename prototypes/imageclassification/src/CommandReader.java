import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CommandReader implements Iterable<Command> {

  private Map<String, Command> validCommands = new HashMap<>();
  private String[] issuedCommands;

  public CommandReader addValidCommand(final String command, final int numberParameters, final Runnable executeWhenIssued) {
    this.validCommands.put(command, new Command(command, numberParameters, executeWhenIssued));
    return this;
  }

  public CommandReader setIssuedCommands(final String[] issuedCommands) {

    return this;
  }

  @Override
  public Iterator<Command> iterator() {
    // TODO Auto-generated method stub
    return null;
  }

}
