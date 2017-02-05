package org.epg;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final String ERROR_RET = "Proper Usage is: java program epg_input_file epg_output_file channel_list_file";

    public static void main( String[] args ) throws Exception {

        if (args.length!=3) {
            System.out.println(ERROR_RET);
            System.exit(0);
        }

        String epgInputFileName = args[0];
        String epgOutputFileName = args[1];
        String channelListFileName = args[2];

        EpgChannelFilter epgChannelFilter = new EpgChannelFilter(epgInputFileName, epgOutputFileName, channelListFileName);

        epgChannelFilter.execute();

    }
}
