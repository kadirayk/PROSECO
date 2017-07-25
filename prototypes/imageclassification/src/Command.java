
public class Command {

  private String param;
  private int numberParameters;
  private Runnable executable;

  public Command(final String command, final int numberParameters, final Runnable executeWhenIssued) {
    this.param = command;
    this.numberParameters = numberParameters;
    this.executable = executeWhenIssued;
  }

  public String getParam() {
    return this.param;
  }

  public int getNumberOfParameters() {
    return this.numberParameters;
  }

  public Runnable getRunnable() {
    return this.executable;
  }

}
