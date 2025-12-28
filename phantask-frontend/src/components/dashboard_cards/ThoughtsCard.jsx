import React, { useState, useEffect } from 'react';
import LoadingSkeleton from '../LoadingSkeleton';


const ThoughtsCard = () => {
    const [quote, setQuote] = useState(null);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [error, setError] = useState(false);


    // Expanded fallback quotes array with 24+ quotes
    const fallbackQuotes = [
        {
            text: "Any fool can write code that a computer can understand. Good programmers write code that humans can understand.",
            author: "Martin Fowler"
        },
        {
            text: "The best way to predict the future is to invent it.",
            author: "Alan Kay"
        },
        {
            text: "Code is like humor. When you have to explain it, it's bad.",
            author: "Cory House"
        },
        {
            text: "First, solve the problem. Then, write the code.",
            author: "John Johnson"
        },
        {
            text: "Experience is the name everyone gives to their mistakes.",
            author: "Oscar Wilde"
        },
        {
            text: "Innovation distinguishes between a leader and a follower.",
            author: "Steve Jobs"
        },
        {
            text: "The only way to do great work is to love what you do.",
            author: "Steve Jobs"
        },
        {
            text: "Simplicity is the soul of efficiency.",
            author: "Austin Freeman"
        },
        {
            text: "Talk is cheap. Show me the code.",
            author: "Linus Torvalds"
        },
        {
            text: "Make it work, make it right, make it fast.",
            author: "Kent Beck"
        },
        {
            text: "Programs must be written for people to read, and only incidentally for machines to execute.",
            author: "Harold Abelson"
        },
        {
            text: "The best error message is the one that never shows up.",
            author: "Thomas Fuchs"
        },
        {
            text: "Debugging is twice as hard as writing the code in the first place.",
            author: "Brian Kernighan"
        },
        {
            text: "It's not a bug – it's an undocumented feature.",
            author: "Anonymous"
        },
        {
            text: "Code never lies, comments sometimes do.",
            author: "Ron Jeffries"
        },
        {
            text: "The function of good software is to make the complex appear to be simple.",
            author: "Grady Booch"
        },
        {
            text: "Testing leads to failure, and failure leads to understanding.",
            author: "Burt Rutan"
        },
        {
            text: "Before software can be reusable it first has to be usable.",
            author: "Ralph Johnson"
        },
        {
            text: "Perfection is achieved not when there is nothing more to add, but when there is nothing left to take away.",
            author: "Antoine de Saint-Exupéry"
        },
        {
            text: "Good code is its own best documentation.",
            author: "Steve McConnell"
        },
        {
            text: "The most damaging phrase in the language is: 'We've always done it this way.'",
            author: "Grace Hopper"
        },
        {
            text: "Controlling complexity is the essence of computer programming.",
            author: "Brian Kernighan"
        },
        {
            text: "Walking on water and developing software from a specification are easy if both are frozen.",
            author: "Edward V. Berard"
        },
        {
            text: "The most important property of a program is whether it accomplishes the intention of its user.",
            author: "C.A.R. Hoare"
        }
    ];


    // Get random fallback quote
    const getRandomFallbackQuote = () => {
        return fallbackQuotes[Math.floor(Math.random() * fallbackQuotes.length)];
    };


    // Fetch quote from DummyJSON API (confirmed working)
    const fetchQuote = async () => {
        try {
            setLoading(true);
            setError(false);

            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 5000);

            const response = await fetch(
                'https://dummyjson.com/quotes/random',
                { signal: controller.signal }
            );

            clearTimeout(timeoutId);

            if (!response.ok) {
                throw new Error('API response not OK');
            }

            const data = await response.json();

            setQuote({
                text: data.quote,
                author: data.author
            });
        } catch (err) {
            setError(true);
            setQuote(getRandomFallbackQuote());
        } finally {
            setLoading(false);
        }
    };


    useEffect(() => {
        fetchQuote();
    }, []);


    if (loading) {
        return <LoadingSkeleton rows={3} hasButton={true} />;
    }


    const getNewQuote = () => {
        fetchQuote();
    };


    const isLongQuote = quote?.text.length > 120;


    return (
        <>
            {/* Card */}
            <div className="w-full h-full rounded-xl border-2 border-[#522320] bg-[#ffffff] p-3 shadow-md shadow-[#522320]/20 transition-all duration-300 hover:shadow-xl hover:shadow-[#522320]/30 hover:-translate-y-0.5 flex flex-col">
                <span className='w-full h-full flex flex-col justify-between'>


                    <h2 className="h-10 text-lg font-semibold py-1 text-[#522320] text-center">
                        Daily Inspiration
                    </h2>


                    <main className='w-full h-full flex flex-col justify-center items-center'>
                        <div className="w-14 h-14 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-3 shadow-md shadow-[#522320]/10">
                            <span className="text-3xl">💡</span>
                        </div>


                        <div className="text-center w-full">
                            <p className="text-[#522320] text-sm font-medium leading-relaxed italic line-clamp-3">
                                "{quote?.text}"
                            </p>
                            <p className="text-[#522320]/70 text-xs font-semibold mt-2">
                                — {quote?.author}
                            </p>
                        </div>


                        {isLongQuote && (
                            <button
                                onClick={() => setShowModal(true)}
                                className="text-[#522320] text-xs underline hover:text-[#42260b] mt-2"
                            >
                                Read full quote
                            </button>
                        )}
                    </main>


                    <button
                        onClick={getNewQuote}
                        className="w-full text-center hover:rounded-xl text-[#42260b] text-sm font-medium mt-1 py-2 hover:bg-red-100 transition-colors"
                    >
                        Get New Quote
                    </button>
                </span>
            </div>


            {/* Modal */}
            {showModal && (
                <div className="fixed inset-0 z-50 flex items-center justify-center">
                    {/* Background Overlay */}
                    <div className="absolute inset-0 bg-black/40" onClick={() => setShowModal(false)} />


                    {/* Modal Container - Responsive width with scroll */}
                    <div className="relative w-[90%] sm:w-[80%] md:w-2/5 max-h-[95vh] animate-slideUp">
                        <div className="bg-white rounded-xl p-4 md:p-6 shadow-xl flex flex-col border border-red-700/30 max-h-[95vh] overflow-y-auto">


                            {/* Header Section */}
                            <div className="mb-3 text-center flex-shrink-0">
                                <h3 className="text-2xl font-bold text-amber-950">Daily Inspiration</h3>
                                <p className="text-sm text-gray-700 mt-1">
                                    {error ? 'A quote from our collection' : 'A quote to inspire your journey'}
                                </p>
                            </div>


                            {/* Body Section - Quote Content */}
                            <div className="flex-1 flex flex-col items-center justify-center py-4">
                                <div className="w-16 h-16 bg-[#522320]/5 rounded-2xl flex items-center justify-center mb-6 shadow-md shadow-[#522320]/10">
                                    <span className="text-4xl">💡</span>
                                </div>


                                <blockquote className="text-center">
                                    <p className="text-[#522320] text-lg font-medium leading-relaxed mb-4 italic">
                                        "{quote?.text}"
                                    </p>
                                    <footer className="text-[#522320]/70 text-base font-semibold">
                                        — {quote?.author}
                                    </footer>
                                </blockquote>
                            </div>


                            {/* Footer Section - Action Buttons */}
                            <div className="mt-4 flex-shrink-0 flex gap-2">
                                <button
                                    onClick={() => {
                                        setShowModal(false);
                                        getNewQuote();
                                    }}
                                    className="flex-1 py-2 rounded-lg bg-orange-600 hover:bg-orange-700 text-white font-semibold hover:scale-95 transition-transform duration-300 shadow"
                                >
                                    Get New Quote
                                </button>


                                <button
                                    onClick={() => setShowModal(false)}
                                    className="flex-1 py-2 rounded-lg bg-stone-200 hover:bg-stone-300 text-gray-800 font-semibold hover:scale-95 transition-transform duration-300 shadow"
                                >
                                    Close
                                </button>
                            </div>
                        </div>
                    </div>


                    {/* Custom Styles - Animations and Scrollbar */}
                    <style>
                        {`
                            /* Slide-up animation for modal entrance */
                            @keyframes slideUp {
                                0% { transform: translateY(100%); opacity: 0; }
                                100% { transform: translateY(0); opacity: 1; }
                            }
                            .animate-slideUp {
                                animation: slideUp 0.2s ease-out forwards;
                            }


                            /* Custom scrollbar styling for modal */
                            .overflow-y-auto::-webkit-scrollbar {
                                width: 8px;
                            }


                            .overflow-y-auto::-webkit-scrollbar-track {
                                background: transparent;
                                margin: 0.4rem 0;
                            }


                            .overflow-y-auto::-webkit-scrollbar-thumb {
                                background: #d1d5db;
                                border-radius: 8px;
                            }


                            .overflow-y-auto::-webkit-scrollbar-thumb:hover {
                                background: #9ca3af;
                            }
                        `}
                    </style>
                </div>
            )}
        </>
    );
};


export default ThoughtsCard;
