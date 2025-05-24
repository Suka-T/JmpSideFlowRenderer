import jmp.JMPLoader;
import plg.JmpSideFlowRenderer;
import std.StandAlonePluginInvoker;

public class JmpSideFlowRenderer_std {
    public static void main(String[] args) {
        JMPLoader.UsePluginDirectory = false;
        JMPLoader.UseConfigFile = false;
        JMPLoader.UseHistoryFile = false;
        JMPLoader.UseSkinFile = false;

        StandAlonePluginInvoker.exec(args, new JmpSideFlowRenderer());
    }
}
