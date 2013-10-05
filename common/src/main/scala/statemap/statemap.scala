//
// The contents of this file are subject to the Mozilla Public
// License Version 1.1 (the "License"); you may not use this file
// except in compliance with the License. You may obtain a copy
// of the License at http://www.mozilla.org/MPL/
//
// Software distributed under the License is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
// implied. See the License for the specific language governing
// rights and limitations under the License.
//
// The Original Code is State Machine Compiler (SMC).
//
// The Initial Developer of the Original Code is Charles W. Rapp.
// Portions created by Charles W. Rapp are
// Copyright (C) 2000 - 2005 Charles W. Rapp.
// All Rights Reserved.
//
// Port to Scala by Francois Perrad, francois.perrad@gadz.org
// Copyright 2008, Francois Perrad.
// All Rights Reserved.
//
// Contributor(s):
//
// RCS ID
// $Id: statemap.scala,v 1.9 2012/05/13 21:31:13 fperrad Exp $
//
// CHANGE LOG
// (See the bottom of this file.)
//

package statemap

import scala.collection.mutable.Stack
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.PrintStream

class StateUndefinedException() extends RuntimeException() {
}

class TransitionUndefinedException(reason: String) extends RuntimeException(reason) {
}

abstract class FSMContext[State] extends Serializable {
  private var _state: State = _
  private var _lastState = _state
  private var _stateStack: Stack[State] = new Stack[State]
  protected var _transition: String = ""
  private var _debugFlag: Boolean = false
  private var _debugStream: PrintStream = System.err
  private var _listeners: PropertyChangeSupport = new PropertyChangeSupport(this)
  private var _isInTransaction: Boolean = true

  def enterStartState(): Unit

  def getDebugFlag(): Boolean = _debugFlag

  def setDebugFlag(flag: Boolean): Unit = {
    _debugFlag = flag
  }

  def getDebugStream(): PrintStream = _debugStream

  def setDebugStream(stream: PrintStream): Unit = {
    _debugStream = stream
  }

  def getTransition(): String = _transition

  // Is this state machine in a transition? If state is null,
  // then true; otherwise, false.
  def isInTransition(): Boolean = _isInTransaction

  def setState(state: State): Unit = {
    val previousState = _state
    if (_debugFlag)
      _debugStream.println("ENTER STATE     : " + state)
    _state = state
    _lastState = state
    _isInTransaction = false
    // Inform all listeners about this state change
    _listeners.firePropertyChange("State", previousState, _state)
  }

  def getLastState(): State = {
    return _lastState
  }

  def getState(): State = {
    if (_isInTransaction)
      throw new StateUndefinedException()
    return _state
  }

  def clearState(): Unit = {
    _isInTransaction = true
  }

  def pushState(state: State): Unit = {
    val previousState = _state
    if (_state == null)
      throw new NullPointerException("uninitialized state")
    if (_isInTransaction)
      throw new StateUndefinedException()
    if (_debugFlag)
      _debugStream.println("PUSH TO STATE   : " + state)
    _stateStack.push(_state)
    _state = state
    // Inform all listeners about this state change
    _listeners.firePropertyChange("State", previousState, _state)
  }

  def popState(): Unit = {
    if (_stateStack.length == 0) {
      if (_debugFlag)
        _debugStream.println("POPPING ON EMPTY STATE STACK.")
      throw new NoSuchElementException("empty state stack")
    }
    val previousState = _state
    _state = _stateStack.pop()
    _isInTransaction = false
    if (_debugFlag)
      _debugStream.println("POP TO STATE    : " + _state)
    // Inform all listeners about this state change
    _listeners.firePropertyChange("State", previousState, _state)
  }

  def emptyStateStack(): Unit = {
    _stateStack = new Stack[State]
  }

  def addStateChangeListener(listener: PropertyChangeListener): Unit = {
    _listeners.addPropertyChangeListener("State", listener)
  }

  def removeStateChangeListener(listener: PropertyChangeListener): Unit = {
    _listeners.removePropertyChangeListener("State", listener)
  }
}

//
// CHANGE LOG
// $Log: statemap.scala,v $
// Revision 1.9  2012/05/13 21:31:13  fperrad
// fix deprecation with Scala 2.9.1
//
// Revision 1.8  2010/09/11 19:00:45  fperrad
// use the same message in all language
//
// Revision 1.7  2010/03/15 21:18:01  fperrad
// fix Scala runtime library
//
// Revision 1.6  2009/11/24 20:42:39  cwrapp
// v. 6.0.1 update
//
// Revision 1.5  2009/04/23 13:12:08  fperrad
// Added enterStartState method
//
// Revision 1.4  2008/05/20 18:31:14  cwrapp
// ----------------------------------------------------------------------
//
// Committing release 5.1.0.
//
// Modified Files:
// 	Makefile README.txt smc.mk tar_list.txt bin/Smc.jar
// 	examples/Ant/EX1/build.xml examples/Ant/EX2/build.xml
// 	examples/Ant/EX3/build.xml examples/Ant/EX4/build.xml
// 	examples/Ant/EX5/build.xml examples/Ant/EX6/build.xml
// 	examples/Ant/EX7/build.xml examples/Ant/EX7/src/Telephone.java
// 	examples/Java/EX1/Makefile examples/Java/EX4/Makefile
// 	examples/Java/EX5/Makefile examples/Java/EX6/Makefile
// 	examples/Java/EX7/Makefile examples/Ruby/EX1/Makefile
// 	lib/statemap.jar lib/C++/statemap.h lib/Java/Makefile
// 	lib/Php/statemap.php lib/Scala/Makefile
// 	lib/Scala/statemap.scala net/sf/smc/CODE_README.txt
// 	net/sf/smc/README.txt net/sf/smc/Smc.java
// ----------------------------------------------------------------------
//
// Revision 1.3  2008/02/11 07:23:42  fperrad
// Scala : refactor
//
// Revision 1.2  2008/02/06 09:35:23  fperrad
// Scala 2.3 -> 2.6
//
// Revision 1.1  2008/02/04 10:48:47  fperrad
// + Added Scala library
//
//
