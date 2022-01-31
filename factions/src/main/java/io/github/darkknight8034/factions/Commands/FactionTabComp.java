package io.github.darkknight8034.factions.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.ArrayList;

public class FactionTabComp implements TabCompleter
{
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {

        List<String> vals = new ArrayList<String> ();

        if (args.length == 1)
        {

            vals.add("create");
            vals.add("list");

            return vals;

        }

        return null;

    }

}
