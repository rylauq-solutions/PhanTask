import React from 'react'

const MainContent = ({ children }) => {
    return (
        <div className="flex-1 flex flex-col">
            <main className="flex-1 p-8 pt-14 overflow-y-auto md:pt-6">
                {children || (
                    <div className="max-w-3xl">
                        <h1 className="text-3xl font-bold text-[#5E2E14] mb-1">
                            Welcome to PhanTask Dashboard
                        </h1>
                        <p className="text-[#7B3F1C] text-base">
                            Select a page from the sidebar to get started.
                        </p>
                    </div>
                )}
            </main>
        </div>
    )
}

export default MainContent