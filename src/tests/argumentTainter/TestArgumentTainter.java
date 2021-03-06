package tests.argumentTainter;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.IndexHits;

import output.neo4j.readWriteDB.QueryUtils;
import tests.TestDBTestReadWriteDB;
import tools.argumentTainter.DDGPatcher;
import tools.argumentTainter.DefUseCFGPatcher;
import tools.argumentTainter.DefUseCFGPatcher.DefUseLink;
import tools.ddg.DefUseCFGFactories.DefUseCFG;
import tools.ddg.DefUseCFGFactories.DefUseCFGFactory;
import tools.ddg.DefUseCFGFactories.ReadWriteDbFactory;

public class TestArgumentTainter extends TestDBTestReadWriteDB
{
	private DefUseCFGFactory defUseGraphFactory = new ReadWriteDbFactory();
	
	@Test
	public void testDefUseCFGPatcher()
	{
		Long funcId = getFunctionIdByFunctionName("arg_tainter_basic_test");
		List<Node> basicBlocksToPatch = getBasicBlocksToPatch(funcId, "memset");
		
		DefUseCFGPatcher defUseCFGPatcher = new DefUseCFGPatcher();
		DefUseCFG defUseCFG = defUseGraphFactory.create(funcId);
		
		defUseCFGPatcher.setSourceToPatch("memset", 0);
		defUseCFGPatcher.patchDefUseCFG(defUseCFG, basicBlocksToPatch);
	
		Collection<DefUseLink> defUseLinksToAdd = defUseCFGPatcher.getDefUseLinksToAdd();
		
		assertTrue(defUseLinksToAdd.size() == 1);
		for( DefUseLink a : defUseLinksToAdd){
			assertTrue(a.symbol.equals("myVar"));
		}
		
	}

	@Test
	public void testDDGPatcher()
	{
		
	}
	
	private List<Node> getBasicBlocksToPatch(Long funcId, String source)
	{
		List<Node> basicBlocksToPatch = new LinkedList<Node>();
		List<Node> callNodes = QueryUtils.getCallsToForFunction(source, funcId);	
		for(Node callNode : callNodes){
			basicBlocksToPatch.add(QueryUtils.getBasicBlockForASTNode(callNode));
		}
		return basicBlocksToPatch;
	}
	
	private Long getFunctionIdByFunctionName(String functionName)
	{
		IndexHits<Node> hits = QueryUtils.getFunctionsByName(functionName);
		Long funcId = hits.next().getId();
		return funcId;
	}
}
