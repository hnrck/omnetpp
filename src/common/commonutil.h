//=========================================================================
//  COMMONUTIL.H - part of
//                  OMNeT++/OMNEST
//           Discrete System Simulation in C++
//
//  Author: Andras Varga
//
//=========================================================================

/*--------------------------------------------------------------*
  Copyright (C) 2006-2015 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  `license' for details on this and other legal matters.
*--------------------------------------------------------------*/

#ifndef __OMNETPP_COMMON_COMMONUTIL_H
#define __OMNETPP_COMMON_COMMONUTIL_H

#include <cassert>
#include <stdint.h> // <cstdint> is C++11
#include <iostream>
#include <sstream>
#include <iomanip>
#include "commondefs.h"
#include "exception.h"

namespace omnetpp {
namespace common {

#ifdef NDEBUG
#  define Assert(x)
#  define DBG(x)
#else
#  define Assert(expr)  ((void) ((expr) ? 0 : (throw opp_runtime_error("ASSERT: condition %s false, %s line %d", \
                        #expr, __FILE__, __LINE__), 0)))
//#  define DBG(x)  printf x
#  define DBG(x)
#endif


COMMON_API extern const double NaN;
COMMON_API extern const double POSITIVE_INFINITY;
COMMON_API extern const double NEGATIVE_INFINITY;

inline bool isNaN(double d) { return d != d;}
inline bool isPositiveInfinity(double d) { return d==POSITIVE_INFINITY; }
inline bool isNegativeInfinity(double d) { return d==NEGATIVE_INFINITY; }


#ifdef _MSC_VER
#ifndef vsnprintf
#define vsnprintf _vsnprintf
#endif
#endif


#define VSNPRINTF(buffer, buflen, formatarg) \
    VSNPRINTF2(buffer, buflen, formatarg, formatarg)

#define VSNPRINTF2(buffer, buflen, lastarg, format) \
    va_list va; \
    va_start(va, lastarg); \
    vsnprintf(buffer, buflen, format, va); \
    buffer[buflen-1] = '\0'; \
    va_end(va);

/**
 * Sets locale to Posix ("C"). Needed because we want to read/write real numbers
 * with "." as decimal separator always (and not "," used by some locales).
 * This affects sprintf(), strtod(), etc.
 */
COMMON_API void setPosixLocale();

/**
 * A more convenient gethostname(). Does its best, but when it fails the result will be nullptr.
 */
COMMON_API const char *opp_gethostname();

/**
 * Debugging aid: prints a message on entering/leaving methods; message
 * gets indented according to call depth. See TRACE_CALL macro.
 */
template <class T> struct ToString;

class COMMON_API CallTracer
{
  private:
    static int depth;
    std::string funcname;
    std::string result;
  public:
    CallTracer(const char *fmt,...);
    ~CallTracer();
    static void printf(const char *fmt, ...);
    static void setDepth(int d) {depth = d;}
    template <class T> void setResult(T x) { result = ToString<T>::toString(x); };
};

// helper class because template method overloading sucks
// see: http://www.gotw.ca/publications/mill17.htm

template <class T> struct ToString
{
    static std::string toString(const T x) { std::ostringstream s; s << x; return s.str();}
};

template <class T> struct ToString<T*>
{
    static std::string toString(const T* x) { std::ostringstream s; s << ((void*)x); return s.str(); }
};

#define TRACE_CALL  CallTracer __x

#define TPRINTF CallTracer::printf

#define RETURN(x) { __x.setResult(x); return x; }

/**
 * Performs the RDTSC (read time stamp counter) x86 instruction, and returns
 * the resulting high-resolution 64-bit timer value. This can be used for
 * ad-hoc performance measurements on Windows (this function returns 0 on
 * other platforms).
 *
 * See http://en.wikipedia.org/wiki/RDTSC
 */
uint64_t readCPUTimeStampCounter();


/**
 * Not all our bison/flex based parsers are reentrant. This macro is meant
 * to catch and report concurrent invocations, e.g. from background threads
 * in the GUI code.
 */
#define NONREENTRANT_PARSER() \
    static bool active = false; \
    struct Guard { \
      Guard() {if (active) throw opp_runtime_error("non-reentrant parser invoked again while parsing"); active=true;} \
      ~Guard() {active=false;} \
    } __guard;

} // namespace common
}  // namespace omnetpp

#endif
