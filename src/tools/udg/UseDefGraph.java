package tools.udg;

import java.util.List;

import tools.udg.environments.UseDefEnvironment;
import misc.MultiHashMap;

public class UseDefGraph {
	
	// A UseDefGraph is a table indexed
	// by identifiers. Each table-entry
	// is a list of the UseOrDefRecords
	// of the identifier.
	
	MultiHashMap useOrDefRecordTable = new MultiHashMap();
	
	public MultiHashMap getUseDefDict()
	{
		return useOrDefRecordTable;
	}
	
	public List<Object> getUsesAndDefsForSymbol(String symbol)
	{
		return useOrDefRecordTable.getListForKey(symbol);
	}
	
	public void addDefinition(String identifier, long nodeId)
	{
		add(identifier, nodeId, true);
	}

	public void addUse(String identifier, long nodeId)
	{
		add(identifier, nodeId, false);
	}
	
	private void add(String identifier, long basicBlockId, boolean isDef)
	{
		UseOrDefRecord record = new UseOrDefRecord(basicBlockId, isDef);		
		useOrDefRecordTable.add(identifier, record);
	}
		
}
