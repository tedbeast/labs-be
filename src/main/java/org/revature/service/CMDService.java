package org.revature.service;

import org.revature.App;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class CMDService {
    /**
     * run a cmd command and convert the entire output to a string
     * @param cmd
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public String runCommandReturnOutput(String cmd) throws IOException, InterruptedException {
        App.log.info("running this command: "+cmd);
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        String result = "";
        String outputLine = "";
        do{
            outputLine = reader.readLine();
            if(outputLine!=null)
                result += outputLine + "\n";
        }while(outputLine!=null);
        process.waitFor();

        return result;
    }
}
