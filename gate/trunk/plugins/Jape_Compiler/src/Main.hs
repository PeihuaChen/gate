-----------------------------------------------------------------------------
-- |
-- Module      : Main
-- Copyright   : (C) Ontotext 2005
-- License     : LGPL
--
-- Maintainer  : Krasimir Angelov <kr.angelov@sirma.bg>
-- Stability   : beta
-- Portability : portable
--
-----------------------------------------------------------------------------

module Main(main) where

import Japec.CmdFlags
import Japec.FilePath
import Japec.AbsSyntax
import Japec.AbsSyntax.Lexer
import Japec.AbsSyntax.Parser
import Japec.TSyntax
import Japec.TranslateGrammar
import Japec.OptimizeTransducer
import Japec.CodeGen
import Japec.CodeGen.Java
import StringUtils.KeyString as KS(unpack)
import StringUtils.StringBuffer as SB(readFile)
import Text.PrettyPrint
import System.Environment
import System.Exit
import System.IO
import System.Process
import System.Directory
import System.Posix.Internals

main :: IO ()
main = do
  args <- getArgs
  let (flags, files, errs) = parseCmdFlags args
  if not (null errs)
    then mapM_ (hPutStrLn stderr) errs
    else if showHelp flags || null files
           then do 
             prgName <- getProgName
             hPutStrLn stderr (japecUsageInfo prgName)
           else mapM_ (processGrammar flags) files

processGrammar flags fpath = do
--  hPutStr stderr ("Reading "++fpath++" ...")
  s <- SB.readFile fpath
  case runAlex s parseJapeGrammar of
    Left s         -> do hPutStrLn stderr ("\n\n"++s)
                         hFlush stderr
                         exitWith (ExitFailure 1)
    Right jgrammar -> do let (dir,fname,_) = splitFilePath fpath
                         time <- if tempNames flags
                                   then do pid <- c_getpid
                                           return ('_':show pid)
                                   else return ""
                         g <- readGrammar dir fname time jgrammar
                         let (missing, t) = translateGrammar g
                             optimized_t  = optimizeTransducer (finalPhase flags) t
                         createDirectoryIfMissing True (outputDir flags)
                         if null missing
                           then do let phase_names = map getPhaseName optimized_t
                                   writeFile (outputDir flags `joinFileName` "phases") (unlines phase_names)
                                   mapM_ (dumpTransducerPhase flags) optimized_t
                           else hPutStrLn stderr (show missing)

readGrammar dir fname time (MultiPhase_ name phases) = do
--  hPutStrLn stderr (" ("++KS.unpack name++")")
  gs <- loop [] phases
  return (MultiPhase ("Grammar_"++fname) gs)
  where
    loop gs []             = return (reverse gs)
    loop gs (gname:gnames) = do
      let fpath = dir `joinFileName` (KS.unpack gname) `joinFileExt` "jape"
--      hPutStr stderr ("Reading "++fpath++" ...")
      s <- SB.readFile fpath
      case runAlex s parseJapeGrammar of
        Left s         -> do hPutStrLn stderr ("\n\n"++s)
                             hFlush stderr
                             exitWith (ExitFailure 1)
        Right jgrammar -> do let (_,fname,_) = splitFilePath fpath
                             g <- readGrammar dir fname time jgrammar
                             loop (g:gs) gnames
readGrammar dir fname time (SinglePhase_ name input control debug match rules) = do
--  hPutStrLn stderr (" ("++KS.unpack name++")")
  return (SinglePhase ("Phase_"++fname++time) input control debug match rules)

dumpTransducerPhase flags@(CmdFlags{outputFormat=Dot}) g@(TransducerPhase name input control debug match tgraph) = do  
  let fpath = (outputDir flags) `joinFileName` name `joinFileExt` "dot"
--  hPutStr stderr ("\nWritting "++fpath++" ...")
  writeFile fpath (render (ppTransitionGraph g))
dumpTransducerPhase flags@(CmdFlags{outputFormat=PS}) g@(TransducerPhase name input control debug match tgraph) = do
  let fpath = (outputDir flags) `joinFileName` name `joinFileExt` "ps"
--  hPutStr stderr ("\nWritting "++fpath++" ...")
  (hIn,hOut,hErr,pid) <- runInteractiveProcess "dot" ["-Tps","-o",fpath] Nothing Nothing
  hPutStr hIn (render (ppTransitionGraph g))
  hFlush hIn
  -- waitForProcess pid
  return ()
dumpTransducerPhase flags@(CmdFlags{outputFormat=Jpeg}) g@(TransducerPhase name input control debug match tgraph) = do
  let fpath = (outputDir flags) `joinFileName` name `joinFileExt` "jpeg"
--  hPutStr stderr ("\nWritting "++fpath++" ...")
  (hIn,hOut,hErr,pid) <- runInteractiveProcess "dot" ["-Tjpeg","-o",fpath] Nothing Nothing
  hPutStr hIn (render (ppTransitionGraph g))
  hFlush hIn
  -- waitForProcess pid
  return ()
dumpTransducerPhase flags@(CmdFlags{outputFormat=Japec}) g@(TransducerPhase name input control debug match tgraph) = do
  let fpath = (outputDir flags) `joinFileName` name `joinFileExt` "japec"
--  hPutStr stderr ("\nWritting "++fpath++" ...")
  writeFile fpath (render (ppStateSwitch (codeGen g)))
dumpTransducerPhase flags@(CmdFlags{outputFormat=Java}) g@(TransducerPhase name input control debug match tgraph) = do
  let fpath = (outputDir flags) `joinFileName` name `joinFileExt` "java"
--  hPutStr stderr ("\nWritting "++fpath++" ...")
  writeFile fpath (render (ppTransducerPhaseJavaCode flags g))
