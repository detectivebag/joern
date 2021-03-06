package tools.ddg;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import tools.ddg.DefUseCFGFactories.DefUseCFGFactory;
import tools.ddg.DefUseCFGFactories.DefUseCFG;
import tools.ddg.DefUseCFGFactories.BatchInserterFactory;
import misc.HashMapOfSets;


public class DDGCreator {
	
	DefUseCFG cfg;
	DefUseCFGFactory cfgFactory = new BatchInserterFactory();
	
	HashMapOfSets in = new HashMapOfSets();
	HashMapOfSets out = new HashMapOfSets();
	HashMapOfSets gen = new HashMapOfSets();
	HashSet<Long> changedNodes;
	
	private class Definition{
		public Definition(Long aBasicBlock, String aIdentifier)
		{
			basicBlock = aBasicBlock;
			identifier = aIdentifier;
		}
		
		public Long basicBlock;
		public String identifier;
	};
	
	public void setFactory(DefUseCFGFactory aFactory)
	{
		cfgFactory = aFactory;
	}
	
	public DDG createForFunctionById(Long funcId)
	{
		return createForDefUseCFG(cfgFactory.create(funcId));	
	}

	public DDG createForDefUseCFG(DefUseCFG aCfg)
	{
		cfg = aCfg;
		calculateReachingDefs();
		return createDDGFromReachingDefs();
	}
	
	private void calculateReachingDefs()
	{
		initReachingDefs();
				
		while(!changedNodes.isEmpty()){
			
			Long currentBlock = popFromChangedNodes();
			
			updateIn(currentBlock);
			boolean changed = updateOut(currentBlock);
		
			if(!changed) continue;
						
			List<Object> children = cfg.getChildBlocks().getListForKey(currentBlock);
			if(children == null)
				continue;			
			
			for(Object o: children)
				changedNodes.add((Long) o);
						
		}
		
	}

	private void initReachingDefs()
	{
		initOut();
		initGenFromOut();
		changedNodes = new HashSet<Long>();		
		changedNodes.addAll(cfg.getBasicBlocks());
	}

	private Long popFromChangedNodes()
	{
		Long x = changedNodes.iterator().next();
		changedNodes.remove(x);		
		return x;
	}

	private void initOut()
	{
		for(Long basicBlock : cfg.getBasicBlocks()){
			
			// this has the nice side-effect that an
			// empty hash is created for the basic block.
			
			out.removeAllForKey(basicBlock);
			
			List<Object> symsDefined = cfg.getSymbolsDefined().getListForKey(basicBlock);
			if(symsDefined == null) continue;
			
			for(Object s: symsDefined){
				String symbol = (String) s;
				out.add(basicBlock, new Definition(basicBlock, symbol));
			}
		}
	}

	private void initGenFromOut()
	{
		for(Long basicBlock : cfg.getBasicBlocks()){
			for(Object o: out.getListForKey(basicBlock))
				gen.add(basicBlock, o);		
		}
	}
	
	private void updateIn(Long x)
	{
		List<Object> parents = cfg.getParentBlocks().getListForKey(x);		
		if(parents == null) return;
		
		in.removeAllForKey(x);
		
		// in(x) = union(out(p))_{p in parents(x)}
		for(Object p : parents){
			Long parent = (Long) p;
			HashSet<Object> parentOut = out.getListForKey(parent);
			if(parentOut == null) continue;			
			for (Object o : parentOut)
				in.add(x, o);
		}
	}

	private boolean updateOut(Long x)
	{	
		HashSet<Object> oldOut = new HashSet<Object>(out.getListForKey(x));		
		
		out.removeAllForKey(x);
		
		// in(x)
		HashSet<Object> inForX = in.getListForKey(x);
		if(inForX != null){		
			for(Object o : inForX){
				out.add(x, o);
			}		
		}
		
		// -kill(x)
		List<Object> killX = cfg.getSymbolsDefined().getListForKey(x);
		if(killX != null){
		
			Iterator<Object> it = out.getListForKey(x).iterator();
			while(it.hasNext()){
				Definition def = (Definition) it.next();
				if(killX.contains(def.identifier))
					it.remove();
			}
			
		}	
		
		// gen(X)
		HashSet<Object> genX = gen.getListForKey(x);

		if(genX != null){
			for(Object o :  genX){
				out.add(x, o);
			}
		}
		
		
		return !oldOut.equals(out.getListForKey(x));
	}

	private DDG createDDGFromReachingDefs()
	{
		DDG ddg = new DDG();
		
		for(Long basicBlock : cfg.getBasicBlocks()){
			HashSet<Object> inForBlock = in.getListForKey(basicBlock);
			if(inForBlock == null) continue;			
			List<Object> usedSymbols = cfg.getSymbolsUsed().getListForKey(basicBlock);
			if(usedSymbols == null) continue;
			
			for(Object d : inForBlock){
				Definition def = (Definition) d;
				
				if(usedSymbols.contains(def.identifier))
						ddg.add(def.basicBlock, basicBlock, def.identifier);
			}
		}
	
		return ddg;
	}
	
	
}
