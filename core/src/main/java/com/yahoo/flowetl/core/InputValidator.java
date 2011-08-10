/*******************************************************
 *                                                     *
 * Copyright (C) 2011 Yahoo! Inc. All Rights Reserved. *
 *                                                     *
 *                Licensed under the New               *
 *                 BSD License. See the                *
 *              accompanying LICENSE file              *
 *              for the specific language              *
 *              governing permissions and              *
 *                limitations under the                *
 *                       License.                      *
 *******************************************************/
package com.yahoo.flowetl.core;

import com.yahoo.flowetl.core.pipe.PipeResult;

/**
 * This interface can be used to check a given pipe result for being valid (ie
 * having the needed required or optional params). It is recommended that if the
 * input is not valid a core exception or derivative is thrown.
 * 
 * @author Joshua Harlow
 */
public interface InputValidator
{
    // checks if the input is valid...
    public void checkInput(PipeResult in);
}
