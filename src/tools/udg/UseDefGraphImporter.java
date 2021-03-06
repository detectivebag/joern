package tools.udg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.RelationshipType;

import output.neo4j.EdgeTypes;
import output.neo4j.batchInserter.Neo4JBatchInserter;
import misc.MultiHashMap;



public class UseDefGraphImporter {
	
	long functionId;
	
	public void setFunctionId(long id)
	{
		functionId = id;
	}
	
	public void importGraph(UseDefGraph graph)
	{
		MultiHashMap useDefDict = graph.getUseDefDict();
				
		Iterator<Object> it = useDefDict.getKeySetIterator();
			
		while(it.hasNext()){
			String identifier = (String) it.next();							
			long symbolNodeId = createSymbolNode(identifier);			
			addUseDefEdges(useDefDict, identifier, symbolNodeId);	
		}
	
	}

	private void addUseDefEdges(MultiHashMap useDefDict, String identifier,
			long symbolNodeId)
	{
		
		List<Object> destinations = useDefDict.getListForKey(identifier);			
		
		for(Object d : destinations){
			UseOrDefRecord item = (UseOrDefRecord) d;
			addUseOrDefRecordToDatabase(symbolNodeId, item);			
		}
	}

	private void addUseOrDefRecordToDatabase(long symbolNodeId, UseOrDefRecord item)
	{
		RelationshipType rel;				
		if(item.isDef)
			rel = DynamicRelationshipType.withName(EdgeTypes.DEF);				          
		else
			rel = DynamicRelationshipType.withName(EdgeTypes.USE);
			
		Neo4JBatchInserter.addRelationship(item.nodeId, symbolNodeId, rel, null);
	}

	private long createSymbolNode(String identifier)
	{
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("code", identifier);
		properties.put("type", "Symbol");
		properties.put("functionId", functionId);
		
		long newNodeId = Neo4JBatchInserter.addNode(properties);
		Neo4JBatchInserter.indexNode(newNodeId, properties);
		return newNodeId;
	}
	
}
